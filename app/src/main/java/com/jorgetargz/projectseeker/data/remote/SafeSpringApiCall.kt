package com.jorgetargz.projectseeker.data.remote

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.jorgetargz.projectseeker.data.dto.error.SpringErrorDTO
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class SafeSpringApiCall @Inject constructor(
    private val gson: Gson
) {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    return NetworkResult.Success(body)
                }
            }
            val springErrorDTO: SpringErrorDTO
            response.errorBody()?.let {
                val reader = JsonReader(it.charStream())
                reader.isLenient = true
                try {
                    springErrorDTO = gson.fromJson(reader, SpringErrorDTO::class.java)
                    Timber.e(springErrorDTO.toString())
                    return NetworkResult.Error(springErrorDTO.message, springErrorDTO)
                } catch (e: Exception) {
                    Timber.e(response.errorBody().toString())
                }
            }
            val errorMessage = response.message()
            return NetworkResult.Error(errorMessage)
        } catch (e: Exception) {
            Timber.e(e.toString(), e)
            return NetworkResult.Error(e.message ?: e.toString())
        }
    }
}