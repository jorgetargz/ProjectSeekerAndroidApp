package com.jorgetargz.projectseeker.framework.register_user

import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.functions.FirebaseFunctions
import com.jorgetargz.projectseeker.R
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import com.jorgetargz.projectseeker.network.session.SpringSessionRemoteSource
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class RegisterUserViewModel @Inject constructor(
    private val springSessionRemoteSource: SpringSessionRemoteSource,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFunctions: FirebaseFunctions,
    private val chatClient: ChatClient,
) : BaseViewModel() {

    private val _userRegistered = MutableStateFlow<FirebaseUser?>(null)
    val userRegistered: StateFlow<FirebaseUser?> = _userRegistered

    private val _logged = MutableStateFlow<FirebaseUser?>(null)
    val logged: StateFlow<FirebaseUser?> = _logged

    fun getIDTokenAndLogin() {
        _isLoading.value = true
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result?.token
                if (token != null) {
                    login(token)
                }
            } else {
                _errorResourceCode.value = R.string.error_login
            }
            _isLoading.value = false
        }
    }

    fun login(token : String) {
        viewModelScope.launch {
            val bearerAuth = "Bearer $token"
            springSessionRemoteSource.login(bearerAuth).catch {
                Timber.e(it)
                _errorResourceCode.value = R.string.error_login
                _isLoading.value = false
            }.collect { networkResult ->
                when (networkResult) {
                    is NetworkResult.Loading -> _isLoading.value = true
                    is NetworkResult.Error -> {
                        handleNetworkError(networkResult)
                    }
                    is NetworkResult.Success -> {
                        streamChatLogin()
                    }
                }
            }
        }
    }

    private fun streamChatLogin() {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let {
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

    fun registerUser(
        email: String,
        password: String,
        username: String,
        phoneAuth: PhoneAuthCredential
    ) {
        try {
            this.firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    this.firebaseAuth.currentUser?.let { firebaseUser ->
                        firebaseUser.updatePhoneNumber(phoneAuth)
                            .addOnSuccessListener {
                                Timber.d("Phone number updated")
                                firebaseUser.updateProfile(
                                    UserProfileChangeRequest.Builder()
                                        .setDisplayName(username)
                                        .build()
                                ).addOnSuccessListener {
                                    firebaseUser.sendEmailVerification()
                                    _userRegistered.value = firebaseUser
                                }.addOnFailureListener { e ->
                                    Timber.d("User not updated", e)
                                    _errorResourceCode.value = R.string.user_not_created
                                    firebaseUser.delete()
                                }
                            }
                            .addOnFailureListener { e ->
                                Timber.d("Phone number not updated", e)
                                when (e) {
                                    is FirebaseAuthUserCollisionException -> {
                                        _errorResourceCode.value = R.string.phone_alredy_registered
                                    }

                                    is FirebaseAuthInvalidCredentialsException -> {
                                        _errorResourceCode.value = R.string.invalid_sms_code
                                    }

                                    else -> {
                                        _errorResourceCode.value = R.string.user_not_created
                                    }
                                }
                                firebaseUser.delete()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Timber.d("User not created", e)
                    if (e is FirebaseAuthUserCollisionException) {
                        _errorResourceCode.value = R.string.email_alredy_registered
                    } else {
                        _errorResourceCode.value = R.string.user_not_created
                    }
                }
        } catch (e: FirebaseException) {
            Timber.d("User not registered", e)
        }
    }
}