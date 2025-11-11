package com.medipath.core.services

import com.medipath.core.models.MedicalHistoryRequest
import com.medipath.core.models.UserMedicalHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MedicalHistoryService {
    @GET("/api/users/me/medicalhistory")
    suspend fun getUserMedicalHistory(): Response<UserMedicalHistoryResponse>

    @POST("/api/medicalhistory/add")
    suspend fun addMedicalHistory(@Body comment: MedicalHistoryRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "/api/medicalhistory/{id}")
    suspend fun deleteMedicalHistory(@Path("id") historyId: String): Response<Unit>

    @PUT("/api/medicalhistory/{id}")
    suspend fun updateMedicalHistory(
        @Path("id") commentId: String,
        @Body comment: MedicalHistoryRequest
    ): Response<Unit>
}