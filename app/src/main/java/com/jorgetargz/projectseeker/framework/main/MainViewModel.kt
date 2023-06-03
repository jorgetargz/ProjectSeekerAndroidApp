package com.jorgetargz.projectseeker.framework.main

import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jorgetargz.projectseeker.framework.common.BaseViewModel
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import com.jorgetargz.projectseeker.network.session.SpringSessionRemoteSource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
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
) : BaseViewModel() {

    private val _logoutDone = MutableStateFlow<Boolean?>(null)
    val logoutDone: StateFlow<Boolean?> = _logoutDone

    private val _numberOfUnreadMessages = MutableStateFlow(0)
    val numberOfUnreadMessages: StateFlow<Int> = _numberOfUnreadMessages

    fun logout() {
        viewModelScope.launch {
            firebaseAuth.signOut()
            _logoutDone.value = true
        }
    }

    fun logoutEverywhere() {
        _isLoading.value = true
        viewModelScope.launch {
            firebaseAuth.signOut()
            springSessionRemoteSource.logoutEverywhere().catch { e ->
                _isLoading.value = false
                Timber.e(e.message, e)
            }.collect { networkResult ->
                when (networkResult) {
                    is NetworkResult.Loading -> {
                        _isLoading.value = true
                    }

                    is NetworkResult.Success -> {
                        _isLoading.value = false
                    }

                    is NetworkResult.Error -> {
                        handleNetworkError(networkResult)
                    }
                }
            }
        }
        _logoutDone.value = true
        _isLoading.value = false
    }

    fun checkNumberOfUnReadMessages() {
        viewModelScope.launch {
            chatClient.globalState.totalUnreadCount.collect {
                _numberOfUnreadMessages.value = it
            }
        }
    }

    fun logoutDoneHandled() {
        _logoutDone.value = null
    }
}