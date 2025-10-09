package com.medipath.core.network

import android.content.Context
import com.medipath.core.services.AuthService
import com.medipath.core.services.UserService
import com.medipath.core.services.LocationService
import com.medipath.core.services.SearchService
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

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
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

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }

    val userService: UserService by lazy {
        retrofit.create(UserService::class.java)
    }

    val locationService: LocationService by lazy {
        retrofit.create(LocationService::class.java)
    }

    val searchService: SearchService by lazy {
        retrofit.create(SearchService::class.java)
    }

    fun getSessionManager(): DataStoreSessionManager {
        if (!::dataStoreSessionManager.isInitialized) {
            throw IllegalStateException("RetrofitClient has not been initialized")
        }
        return dataStoreSessionManager
    }
}
