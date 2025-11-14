package com.medipath.core.services

import com.medipath.core.models.VisitsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DoctorService {
    @GET("/api/doctors/me/visits/{date}")
    suspend fun getDoctorVisitsByDate(@Path("date") date: String): Response<VisitsResponse>

    @GET("/api/doctors/me/visits/")
    suspend fun getDoctorVisits(): Response<VisitsResponse>
}
