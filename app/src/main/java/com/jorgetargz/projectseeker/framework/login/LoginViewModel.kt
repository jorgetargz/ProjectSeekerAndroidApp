package com.jorgetargz.projectseeker.framework.login

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.data.UsersRepository
import com.jorgetargz.projectseeker.data.dto.users.AddDeviceDTO
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import com.jorgetargz.projectseeker.network.session.SpringSessionRemoteSource
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.models.PushProvider
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val springSessionRemoteSource: SpringSessionRemoteSource,
    private val usersRepository: UsersRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFunctions: FirebaseFunctions,
    private val firebaseMessaging: FirebaseMessaging,
    private val chatClient: ChatClient,
) : BaseViewModel() {

    // Firebase User State Flow
    private val _firebaseUser = MutableStateFlow(firebaseAuth.currentUser)
    val firebaseUser: StateFlow<FirebaseUser?> = _firebaseUser

    private val _logged = MutableStateFlow<FirebaseUser?>(null) // private mutable live data
    val logged: StateFlow<FirebaseUser?> = _logged

    fun getIDTokenAndLogin() {
        _isLoading.value = true
        firebaseAuth.currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result?.token
                if (token != null) {
                    login(token)
                } else {
                    _errorResourceCode.value = R.string.error_login
                    _isLoading.value = false
                }
            } else {
                _errorResourceCode.value = R.string.error_login
                _isLoading.value = false
            }
        }
    }

    fun login(token: String) {
        viewModelScope.launch {
            val bearerAuth = "Bearer $token"
            springSessionRemoteSource.login(bearerAuth).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_login
                _isLoading.value = false
            }.collect {
                when (it) {
                    is NetworkResult.Loading -> _isLoading.value = true
                    is NetworkResult.Error -> {
                        handleNetworkError(it)
                    }

                    is NetworkResult.Success -> {
                        if (checkIfUserIsRegistered()) {
                            streamChatLogin()
                            sendFCMToken()
                        } else {
                            deleteUnregisteredUser()
                            _errorResourceCode.value = R.string.you_need_to_register
                            _isLoading.value = false
                        }
                    }
                }
            }
        }
    }

    private fun sendFCMToken() {
        viewModelScope.launch {
            val token = firebaseMessaging.token.await()
            val addDeviceDTO = AddDeviceDTO(token)
            usersRepository.addDevice(addDeviceDTO).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_login
                _isLoading.value = false
            }.collect {
                when (it) {
                    is NetworkResult.Loading -> _isLoading.value = true
                    is NetworkResult.Error -> {
                        handleNetworkError(it)
                    }

                    is NetworkResult.Success -> {
                        Timber.d("FCM Token sent")
                        _isLoading.value = false
                    }
                }
            }
            chatClient.addDevice(
                Device(
                    token = token,
                    pushProvider = PushProvider.FIREBASE,
                    providerName = "firebase-push", // Optional, if adding to multi-bundle named provider
                )
            ).enqueue { result ->
                if (result.isSuccess) {
                    Timber.d("Success adding device to Stream Chat")
                } else {
                    Timber.e(result.error().message, result.error())
                }
            }
        }

    }

    private fun deleteUnregisteredUser() {
        val currentUser = firebaseAuth.currentUser
        currentUser?.delete()?.addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.d("Not registered user deleted")
            } else {
                Timber.e(it.exception)
            }
        }
    }

    private fun checkIfUserIsRegistered(): Boolean {
        val currentUser = firebaseAuth.currentUser
        return currentUser?.displayName != null && currentUser.email != null
    }

    private fun streamChatLogin() {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let {
            if (currentUser.displayName == null) {
                _errorResourceCode.value = R.string.you_need_to_register
                _isLoading.value = false
                return
            }
            val streamUser = User(
                id = currentUser.uid,
                image = currentUser.photoUrl.toString(),
                name = currentUser.displayName!!,
            )
            val url = URL("https://europe-west1-hireme-tfg.cloudfunctions.net/ext-auth-chat-getStreamUserToken")
            firebaseFunctions.getHttpsCallableFromUrl(url).call().addOnCompleteListener {
                if (it.isSuccessful) {
                    val token = it.result?.data as String
                    chatClient.connectUser(
                        user = streamUser,
                        token = token
                    ).enqueue { result ->
                        if (result.isSuccess) {
                            Timber.d("Success Connection to Stream Chat")
                            _logged.value = firebaseAuth.currentUser
                            _isLoading.value = false
                        } else {
                            _errorResourceCode.value = R.string.stream_login_error
                            Timber.e(result.error().message.toString())
                            _isLoading.value = false
                        }
                    }
                } else {
                    _errorResourceCode.value = R.string.stream_login_error
                    Timber.e(it.exception)
                    _isLoading.value = false
                }
            }
        }
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        _isLoading.value = true
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInWithCredential:success")
                    val user = this.firebaseAuth.currentUser
                    user?.let { _firebaseUser.value = it }
                } else {
                    // If sign in fails, display a message to the user.
                    _errorResourceCode.value = R.string.authentication_failed
                    Timber.d("signInWithCredential:failure", task.exception)
                }
                _isLoading.value = false
            }
    }

    fun firebaseAuthWithEmailAndPassword(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorResourceCode.value = R.string.error_empty_fields
            return
        }
        _isLoading.value = true
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithEmail:success")
                    this.firebaseAuth.currentUser?.let { _firebaseUser.value = it }
                } else {
                    Timber.w(task.exception, "signInWithEmail:failure")
                    _errorResourceCode.value = R.string.authentication_failed
                }
                _isLoading.value = false
            }
    }

    fun firebaseAuthWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        _isLoading.value = true
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                    task.result?.user?.let { _firebaseUser.value = it }
                } else {
                    Timber.d("signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        _errorResourceCode.value = R.string.invalid_code
                    } else {
                        _errorResourceCode.value = R.string.authentication_failed
                    }
                }
                _isLoading.value = false
            }
    }

    fun firebaseSendPasswordResetEmail(email: String) {
        _isLoading.value = true
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.d("Email sent.")
                    _errorResourceCode.value = R.string.email_sent
                } else {
                    Timber.d("Email not sent.")
                    _errorResourceCode.value = R.string.email_not_sent
                }
                _isLoading.value = false
            }
    }

    fun firebaseUserHandled() {
        _firebaseUser.value = null
    }

    fun userLoggedHandled() {
        _logged.value = null
    }


}