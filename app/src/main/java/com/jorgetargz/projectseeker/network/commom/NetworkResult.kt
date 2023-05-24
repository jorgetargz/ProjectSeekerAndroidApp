package com.jorgetargz.projectseeker.network.commom

import com.jorgetargz.projectseeker.data.dto.error.SpringErrorDTO

sealed class NetworkResult<T>(
    var data: T? = null,
    val message: String? = null,
    val springErrorDTO: SpringErrorDTO? = null
) {

    class Success<T>(data: T) : NetworkResult<T>(data)

    class Error<T>(
        message: String,
        springErrorDTO: SpringErrorDTO? = null
    ) : NetworkResult<T>(null, message, springErrorDTO)

    class Loading<T> : NetworkResult<T>()

    fun <R> map(transform: (data: T) -> R): NetworkResult<R> =
        when (this) {
            is Error -> Error(message?: "Network Error", springErrorDTO)
            is Loading -> Loading()
            is Success -> Success(transform(data!!))
        }

}