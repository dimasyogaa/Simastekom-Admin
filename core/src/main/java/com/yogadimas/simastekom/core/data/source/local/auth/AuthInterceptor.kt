package com.yogadimas.simastekom.core.data.source.local.auth

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authPreferences: AuthPreferences,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = authPreferences.getCachedTokenOnly()
        Log.e("TAG", "AuthInterceptor: $token", )
        val newRequest = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", token)
                .build()
        } else originalRequest


        return chain.proceed(newRequest)
    }
}
