package com.medipath.core.services

import com.medipath.core.models.UserProfileResponse
import com.medipath.core.models.VisitsResponse
import com.medipath.core.models.NotificationsResponse
import com.medipath.core.responses.CodesResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("/api/users/profile")
    suspend fun getUserProfile(@Header("Cookie") cookie: String): UserProfileResponse

    @GET("/api/users/me/visits")
    suspend fun getUpcomingVisits(@Query("upcoming") upcoming: String = "true", @Header("Cookie") cookie: String): VisitsResponse

    @DELETE("/api/visits/{visitid}")
    suspend fun cancelVisit(@Path("visitid") visitId: String, @Header("Cookie") cookie: String): Response<Unit>

    @GET("/api/users/me/codes/{codeType}")
    suspend fun getUserCodes(@Path("codeType") codeType: String?, @Header("Cookie") cookie: String): Response<CodesResponse>

    @GET("/api/users/me/codes")
    suspend fun getAllUserCodes(@Header("Cookie") cookie: String): Response<CodesResponse>

    // TODO: uzywa /profile endpoint, zmienic na inny endpoint
    @GET("/api/users/profile")
    suspend fun getNotificationsFromProfile(@Header("Cookie") cookie: String): UserProfileResponse
}