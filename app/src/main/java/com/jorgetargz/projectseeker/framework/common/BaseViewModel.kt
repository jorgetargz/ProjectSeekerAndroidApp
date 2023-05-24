package com.jorgetargz.projectseeker.framework.common

import androidx.lifecycle.ViewModel
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

open class BaseViewModel : ViewModel() {

    // Error Resource Code State Flow
    protected val _errorResourceCode = MutableStateFlow<Int?>(null)
    val errorResourceCode : StateFlow<Int?> = _errorResourceCode

    // Error String State Flow
    protected val _errorString = MutableStateFlow<String?>(null)
    val errorString : StateFlow<String?> = _errorString

    // Loading State Flow
    protected val _isLoading = MutableStateFlow(false)
    val isLoading : StateFlow<Boolean> = _isLoading

    fun handleNetworkError(it: NetworkResult.Error<*>) {
        _isLoading.value = false
        if (it.springErrorDTO != null) {
            _errorString.value = it.springErrorDTO.message
        } else {
            _errorString.value = it.message
        }
    }

    fun errorStringHandled() {
        _errorString.value = null
    }

    fun errorResourceCodeHandled() {
        _errorResourceCode.value = null
    }

}