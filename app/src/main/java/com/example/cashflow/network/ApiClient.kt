package com.example.cashflow.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // TODO: Replace 192.168.164.97 with your computer's actual IPv4 address.
    // NOTE: Your phone and computer must be on the exact same Wi-Fi network.
    // TIP: If you get connection timeouts, use `ngrok http 5001` and paste the ngrok HTTPS URL here!
    private const val BASE_URL = "http://192.168.164.97:5001/"

    init {
        Log.d("ApiClient", "Initializing Retrofit with Base URL: $BASE_URL")
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
