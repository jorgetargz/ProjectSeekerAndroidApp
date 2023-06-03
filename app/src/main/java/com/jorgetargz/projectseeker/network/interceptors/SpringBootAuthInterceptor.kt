package com.jorgetargz.projectseeker.network.interceptors


import com.google.firebase.auth.FirebaseAuth
import com.jorgetargz.projectseeker.data.shared_preferences.EncryptedSharedPreferencesManager
import com.jorgetargz.projectseeker.network.commom.Config
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import timber.log.Timber
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class SpringBootAuthInterceptor @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val encryptedSharedPreferencesManager: EncryptedSharedPreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        //Get the session cookie from the shared preferences
        val sessionCookie = encryptedSharedPreferencesManager.getString("sessionCookie", null)
        sessionCookie?.let {
            val response = chain.proceed(
                original.newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Cookie", it)
                    .build()
            )
            return if (response.code != 401) {
                response
            } else {
                response.close()
                reAuthAndPerformOriginalRequest(original, chain)
            }
        } ?: run {
            return reAuthAndPerformOriginalRequest(original, chain)
        }
    }

    private fun reAuthAndPerformOriginalRequest(
        original: Request,
        chain: Interceptor.Chain,
    ): Response {
        // If the session cookie is invalid, delete it from the shared preferences
        encryptedSharedPreferencesManager.remove("sessionCookie")

        // Get new session cookie using the firebase id token
        val token: String
        val tokenCompletableFuture: CompletableFuture<String> = CompletableFuture()
        firebaseAuth.currentUser?.getIdToken(false)?.addOnSuccessListener {
            tokenCompletableFuture.complete(it.token!!)
        }?.addOnFailureListener {
            tokenCompletableFuture.completeExceptionally(it)
        }

        // Wait for the token to be retrieved from firebase
        try {
            token = tokenCompletableFuture.get()
        } catch (e: Exception) {
            // If the token could not be retrieved, return the response to the original request
            Timber.e(e.message, e)
            return chain.proceed(original)
        }

        // Make a login request to get the session cookie
        val authRequest: Request.Builder = Request.Builder()
            .url(Config.LOGIN_URL_SPRING_BOOT)
            .method("POST", ByteArray(0).toRequestBody(null, 0, 0))
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $token")
        val authResponse = chain.proceed(authRequest.build())

        // Get the session cookie from the response and save it in the shared preferences if the cookie
        // is not found then returns the response to the original request
        val cookies = authResponse.headers("Set-Cookie")
        val sessionCookie = cookies.firstOrNull { it.contains("session") } ?: return chain.proceed(original)

        // Save the session cookie in the shared preferences
        encryptedSharedPreferencesManager.set("sessionCookie", sessionCookie)

        // Make the original request again with the new session cookie
        val newRequestBuilder: Request.Builder = original.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Cookie", sessionCookie)

        return chain.proceed(newRequestBuilder.build())
    }
}
