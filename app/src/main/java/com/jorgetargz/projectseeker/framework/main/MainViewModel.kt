package com.jorgetargz.projectseeker.framework.main

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import com.jorgetargz.projectseeker.network.session.SpringSessionRemoteSource
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.state
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
    private val springSessionRemoteSource: SpringSessionRemoteSource
) : BaseViewModel() {

    private val _logoutDone = MutableStateFlow(false)
    val logoutDone : StateFlow<Boolean> = _logoutDone

    private val _numberOfUnreadMessages = MutableStateFlow(0)
    val numberOfUnreadMessages : StateFlow<Int> = _numberOfUnreadMessages

    fun logout() {
        chatClient.disconnect(true).enqueue {
            if (it.isSuccess) {
                Timber.d("Successfully disconnected from Stream chat")
            } else {
                Timber.d("Failed to disconnect from Stream chat")
            }
            firebaseAuth.signOut()
            _logoutDone.value = true
        }
    }

    fun logoutEverywhere() {
        _isLoading.value = true
        chatClient.disconnect(false).enqueue { it ->
            if (it.isSuccess) {
                Timber.d("Successfully disconnected from Stream chat")
            } else {
                Timber.d("Failed to disconnect from Stream chat")
            }
            firebaseAuth.signOut()
            viewModelScope.launch {
                springSessionRemoteSource.logoutEverywhere().catch { e ->
                    Timber.d("Failed to logout everywhere", e)
                    _logoutDone.value = true
                    _isLoading.value = false
                }.collect { networkResult ->
                    when (networkResult) {
                        is NetworkResult.Loading -> {
                            _isLoading.value = true
                        }

                        is NetworkResult.Success -> {
                            Timber.d("Success")
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

    fun checkNumberOfUnReadMessages() {
        viewModelScope.launch {
            chatClient.globalState.totalUnreadCount.collect {
                _numberOfUnreadMessages.value = it
            }
        }
    }
}