package com.jorgetargz.projectseeker.network.interceptors

import android.content.Context
import com.jorgetargz.projectseeker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class SkillsAuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = context.resources.getString(R.string.skills_api_key)

        val original = chain.request()

        return chain.proceed(
            original.newBuilder()
                .addHeader("apikey", apiKey)
                .addHeader("Accept", "application/json")
                .build()
        )
    }
}