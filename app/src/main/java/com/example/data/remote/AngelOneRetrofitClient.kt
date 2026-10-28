package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit Network Client Singleton for Angel One SmartAPI
 */
object AngelOneRetrofitClient {

    private const val BASE_URL = "https://apiconnect.angelone.in/"

    // In-memory token store for authenticated requests
    var jwtToken: String? = null
    var apiKey: String? = null
    var feedToken: String? = null
    var clientCode: String? = null

    private val headerInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("X-UserType", "USER")
            .header("X-SourceID", "WEB")
            .header("X-ClientLocalIP", "192.168.1.1")
            .header("X-ClientPublicIP", "106.193.147.98")
            .header("X-MACAddress", "02:00:00:00:00:00")

        jwtToken?.let { token ->
            if (original.header("Authorization") == null && token.isNotBlank()) {
                val bearer = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
                builder.header("Authorization", bearer)
            }
        }

        apiKey?.let { key ->
            if (key.isNotBlank()) {
                if (original.header("X-PrivateKey") == null) {
                    builder.header("X-PrivateKey", key)
                }
                if (original.header("x-api-key") == null) {
                    builder.header("x-api-key", key)
                }
            }
        }

        feedToken?.let { feed ->
            if (original.header("X-FeedToken") == null && feed.isNotBlank()) {
                builder.header("X-FeedToken", feed)
            }
        }

        clientCode?.let { code ->
            if (original.header("X-Client-Code") == null && code.isNotBlank()) {
                builder.header("X-Client-Code", code)
            }
        }

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val api: AngelOneRetrofitApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AngelOneRetrofitApi::class.java)
    }
}
