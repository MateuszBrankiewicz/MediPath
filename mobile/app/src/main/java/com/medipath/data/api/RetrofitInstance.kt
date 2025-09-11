package com.medipath.data.api

import android.content.Context
import com.medipath.utils.MyCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BACKEND_HOST = "10.0.2.2"

    private lateinit var dataStoreSessionManager: DataStoreSessionManager
    private lateinit var myCookieJar: MyCookieJar

    fun initialize(context: Context) {
        dataStoreSessionManager = DataStoreSessionManager(context)
        myCookieJar = MyCookieJar(dataStoreSessionManager, BACKEND_HOST)
    }

    //uzywany do logowania zadania i odpowiedzi HTTP
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY //pokazuje cialo zadania i odpowiedzi
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(myCookieJar)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getSessionManager(): DataStoreSessionManager {
        if (!::dataStoreSessionManager.isInitialized) {
            throw IllegalStateException("RetrofitClient has not been initialized")
        }
        return dataStoreSessionManager
    }
}
