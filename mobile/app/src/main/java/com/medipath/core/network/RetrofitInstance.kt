package com.medipath.core.network

import android.content.Context
import com.medipath.core.services.AuthService
import com.medipath.core.services.UserService
import com.medipath.core.services.LocationService
import com.medipath.core.services.SearchService
import com.medipath.core.services.NotificationsService
import com.medipath.core.services.VisitsService
import com.medipath.core.services.CommentsService
import com.medipath.core.services.CodesService
import com.medipath.core.services.MedicalHistoryService
import com.medipath.core.services.SettingsService
import com.medipath.core.services.DoctorService
import com.medipath.core.utils.SessionCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
     private const val BASE_URL = "https://genitourinary-sunday-superplausibly.ngrok-free.dev"
   private const val WEBSOCKET_URL = "http://192.168.8.100:8080"
//    private const val BASE_URL = "http://10.0.2.2:8080/"
    private lateinit var sessionManager: SharedPreferencesSessionManager
    private lateinit var sessionCookieJar: SessionCookieJar

    fun initialize(context: Context) {
        sessionManager = SharedPreferencesSessionManager(context)
        sessionCookieJar = SessionCookieJar(sessionManager)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun getWebsocketUrl(): String {
        return WEBSOCKET_URL
    }


    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(sessionCookieJar)
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

    val notificationsService: NotificationsService by lazy {
        retrofit.create(NotificationsService::class.java)
    }

    val visitsService: VisitsService by lazy {
        retrofit.create(VisitsService::class.java)
    }

    val commentsService: CommentsService by lazy {
        retrofit.create(CommentsService::class.java)
    }

    val codesService: CodesService by lazy {
        retrofit.create(CodesService::class.java)
    }

    val medicalHistoryService: MedicalHistoryService by lazy {
        retrofit.create(MedicalHistoryService::class.java)
    }

    val settingsService: SettingsService by lazy {
        retrofit.create(SettingsService::class.java)
    }

    val doctorService: DoctorService by lazy {
        retrofit.create(DoctorService::class.java)
    }

    fun getSessionManager(): SharedPreferencesSessionManager {
        if (!::sessionManager.isInitialized) {
            throw IllegalStateException("RetrofitClient has not been initialized")
        }
        return sessionManager
    }
}
