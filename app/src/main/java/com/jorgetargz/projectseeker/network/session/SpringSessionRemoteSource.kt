package com.jorgetargz.projectseeker.network.session

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.jorgetargz.projectseeker.data.dto.error.SpringErrorDTO
import com.jorgetargz.projectseeker.data.shared_preferences.EncryptedSharedPreferencesManager
import com.jorgetargz.projectseeker.network.SessionService
import com.jorgetargz.projectseeker.network.commom.NetworkResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.internal.toHeaderList
import timber.log.Timber
import javax.inject.Inject

class SpringSessionRemoteSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val sessionService: SessionService,
    private val encryptedSharedPreferencesManager: EncryptedSharedPreferencesManager
) {

    fun login(bearerAuth: String): Flow<NetworkResult<Nothing?>> {
        return flow {
            emit(NetworkResult.Loading())
            val response = sessionService.login(bearerAuth)
            if (response.isSuccessful) {
                response.headers().let { headers ->
                    val sessionCookie = headers.toHeaderList().firstOrNull { it.value.toString().contains("session") }
                    sessionCookie?.let {

                        encryptedSharedPreferencesManager.set("sessionCookie", sessionCookie.value.utf8())
                        emit(NetworkResult.Success(null))
                    } ?: run {
                        emit(NetworkResult.Error<Nothing?>("No session cookie found"))
                    }
                }
            } else {
                val springErrorDTO: SpringErrorDTO
                response.errorBody()?.let {
                    val reader = JsonReader(it.charStream())
                    reader.isLenient = true
                    springErrorDTO = gson.fromJson(reader, SpringErrorDTO::class.java)
                    Timber.e(springErrorDTO.toString())
                    emit(NetworkResult.Error<Nothing?>(springErrorDTO.message, springErrorDTO))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun logoutEverywhere(): Flow<NetworkResult<Nothing?>> {
        return flow {
            emit(NetworkResult.Loading())
            val response = sessionService.logoutEverywhere()
            if (response.isSuccessful) {
                emit(NetworkResult.Success(null))
            } else {
                val springErrorDTO: SpringErrorDTO
                response.errorBody()?.let {
                    val reader = JsonReader(it.charStream())
                    reader.isLenient = true
                    springErrorDTO = gson.fromJson(reader, SpringErrorDTO::class.java)
                    Timber.e(springErrorDTO.toString())
                    emit(NetworkResult.Error<Nothing?>(springErrorDTO.message, springErrorDTO))
                }
            }
        }.flowOn(Dispatchers.IO)
    }

}