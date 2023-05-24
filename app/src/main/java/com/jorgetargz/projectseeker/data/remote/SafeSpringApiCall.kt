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
                springErrorDTO = gson.fromJson(reader, SpringErrorDTO::class.java)
                Timber.e(springErrorDTO.toString())
                return NetworkResult.Error(springErrorDTO.message, springErrorDTO)
            }
            val errorMessage = response.message()
            return error(errorMessage)
        } catch (e: Exception) {
            Timber.e(e.toString(), e)
            return error(e.message ?: e.toString())
        }
    }

    private fun <T> error(errorMessage: String): NetworkResult<T> =
        NetworkResult.Error("Api call failed $errorMessage")

}