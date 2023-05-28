package com.jorgetargz.projectseeker.network

import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.POST

interface SessionService {

    @POST("session/login")
    suspend fun login(@Header("Authorization") bearerAuth: String): Response<Unit>

    @POST("session/logout-everywhere")
    suspend fun logoutEverywhere(): Response<Unit>
}