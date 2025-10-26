package com.medipath.core.services

import com.medipath.core.models.VisitDetailsResponse
import com.medipath.core.models.VisitsResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface VisitsService {
    @GET("/api/users/me/visits")
    suspend fun getAllVisits(@Header("Cookie") cookie: String): VisitsResponse

    @GET("/api/users/me/visits")
    suspend fun getUpcomingVisits(@Query("upcoming") upcoming: String = "true", @Header("Cookie") cookie: String): VisitsResponse

    @GET("/api/visits/{id}")
    suspend fun getVisitDetails(@Path("id") visitId: String, @Header("Cookie") cookie: String): VisitDetailsResponse

    @DELETE("/api/visits/{visitid}")
    suspend fun cancelVisit(@Path("visitid") visitId: String, @Header("Cookie") cookie: String): Response<Unit>
}
