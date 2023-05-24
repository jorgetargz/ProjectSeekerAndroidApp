package com.jorgetargz.projectseeker.network.interceptors

import com.jorgetargz.projectseeker.network.commom.Config
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class SkillsAuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = Config.SKILLS_API_KEY

        val original = chain.request()

        return chain.proceed(
            original.newBuilder()
                .addHeader("apikey", apiKey)
                .addHeader("Accept", "application/json")
                .build()
        )
    }
}