package com.yogadimas.simastekom.api


import com.yogadimas.simastekom.BuildConfig
import com.yogadimas.simastekom.BuildConfig.BASE_URL_API
import com.yogadimas.simastekom.progress.ProgressInterceptor
import com.yogadimas.simastekom.progress.ProgressListener
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiConfig {

    companion object {

        fun getApiService(progressListener: ProgressListener): ApiService {

            val loggingInterceptor = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            } else {
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
            }


            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(ProgressInterceptor(progressListener)) // Tambahkan ProgressInterceptor
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL_API)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(ApiService::class.java)

        }

    }
}