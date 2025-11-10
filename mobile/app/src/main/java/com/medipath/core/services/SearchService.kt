package com.medipath.core.services

import com.medipath.core.models.DoctorCommentsResponse
import com.medipath.core.models.InstitutionDetail
import com.medipath.core.models.DoctorScheduleResponse
import com.medipath.core.models.InstitutionDetailResponse
import com.medipath.core.models.InstitutionDoctorsResponse
import com.medipath.core.models.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchService {
    @GET("/api/search/{query}")
    suspend fun search(
        @Path("query") query: String, 
        @Query("type") type: String, 
        @Query("city") city: String? = null, 
        @Query("specialisations") specialisations: String? = null
    ): Response<SearchResponse>
    
    @GET("/api/doctors/{doctorId}/schedules")
    suspend fun getDoctorSchedules(
        @Path("doctorId") doctorId: String,
        @Query("institution") institutionId: String? = null
    ): Response<DoctorScheduleResponse>
    
    @GET("/api/comments/doctor/{id}")
    suspend fun getDoctorComments(
        @Path("id") doctorId: String
    ): Response<DoctorCommentsResponse>

    @GET("/api/institution/{institutionId}/doctors")
    suspend fun getInstitutionDoctors(
        @Path("institutionId") institutionId: String
    ): Response<InstitutionDoctorsResponse>

    @GET("/api/institution/{id}")
    suspend fun getInstitution(
        @Path("id") id: String,
        @Query("fields") fields: List<String>? = null
    ): Response<InstitutionDetailResponse>
}