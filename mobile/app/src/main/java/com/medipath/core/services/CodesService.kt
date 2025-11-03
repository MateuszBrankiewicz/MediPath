package com.medipath.core.services

import com.medipath.core.models.CodeRequest
import retrofit2.Response
import retrofit2.http.*

interface CodesService {
    @PUT("/api/visits/code")
    suspend fun markCodeAsUsed(@Header("Cookie") cookie: String, @Body request: CodeRequest): Response<Unit>

    @DELETE("/api/visits/code")
    suspend fun deleteCode(@Header("Cookie") cookie: String, @Body request: CodeRequest): Response<Unit>
}

