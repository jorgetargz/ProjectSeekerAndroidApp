package com.jorgetargz.projectseeker.framework.main

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jorgetargz.projectseeker.data.shared_preferences.EncryptedSharedPreferencesManager
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import com.jorgetargz.projectseeker.network.session.SpringSessionRemoteSource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.extensions.globalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val chatClient: ChatClient,
    private val firebaseAuth: FirebaseAuth,
    private val springSessionRemoteSource: SpringSessionRemoteSource,
    private val encryptedSharedPreferencesManager: EncryptedSharedPreferencesManager
) : BaseViewModel() {

    private val _logoutDone = MutableStateFlow(false)
    val logoutDone : StateFlow<Boolean> = _logoutDone

    private val _numberOfUnreadMessages = MutableStateFlow(0)
    val numberOfUnreadMessages : StateFlow<Int> = _numberOfUnreadMessages

    fun logout() {
        chatClient.disconnect(true).enqueue {
            logChatClientLogOut(it)
            firebaseAuth.signOut()
            encryptedSharedPreferencesManager.clear()
            _logoutDone.value = true
        }
    }

    fun logoutEverywhere() {
        _isLoading.value = true
        chatClient.disconnect(false).enqueue { it ->
            logChatClientLogOut(it)
            firebaseAuth.signOut()
            encryptedSharedPreferencesManager.clear()
            viewModelScope.launch {
                springSessionRemoteSource.logoutEverywhere().catch { e ->
                    _logoutDone.value = true
                    _isLoading.value = false
                    Timber.e(e.message, e)
                }.collect { networkResult ->
                    when (networkResult) {
                        is NetworkResult.Loading -> {
                            _isLoading.value = true
                        }

                        is NetworkResult.Success -> {
                            _logoutDone.value = true
                            _isLoading.value = false
                        }

                        is NetworkResult.Error -> {
                            handleNetworkError(networkResult)
                        }
                    }
                }
            }
        }
    }

    private fun logChatClientLogOut(it: Result<Unit>) {
        if (it.isSuccess) {
            Timber.d("Successfully disconnected from Stream chat")
        } else {
            Timber.d("Failed to disconnect from Stream chat")
        }
    }

    fun checkNumberOfUnReadMessages() {
        viewModelScope.launch {
            chatClient.globalState.totalUnreadCount.collect {
                _numberOfUnreadMessages.value = it
            }
        }
    }
}