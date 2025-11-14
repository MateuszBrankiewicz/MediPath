package com.medipath.core.services

import com.medipath.core.models.BookingRequest
import com.medipath.core.models.CompleteVisitRequest
import com.medipath.core.models.VisitDetailsResponse
import com.medipath.core.models.VisitsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface VisitsService {
    @GET("/api/users/me/visits")
    suspend fun getAllVisits(): Response<VisitsResponse>

    @GET("/api/users/me/visits")
    suspend fun getUpcomingVisits(@Query("upcoming") upcoming: String = "true"): Response<VisitsResponse>

    @GET("/api/visits/{id}")
    suspend fun getVisitDetails(@Path("id") visitId: String): Response<VisitDetailsResponse>

    @DELETE("/api/visits/{visitid}")
    suspend fun cancelVisit(@Path("visitid") visitId: String): Response<Unit>
    
    @POST("/api/visits/add")
    suspend fun bookAppointment(@Body bookingRequest: BookingRequest): Response<Unit>
    
    @PUT("/api/visits/{visitid}/reschedule")
    suspend fun rescheduleVisit(@Path("visitid") visitId: String, @Query("newschedule") newScheduleId: String): Response<Unit>
    
    @PUT("/api/visits/{visitid}/complete")
    suspend fun completeVisit(@Path("visitid") visitId: String, @Body request: CompleteVisitRequest): Response<Unit>
}