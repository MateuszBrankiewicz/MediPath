package com.medipath.core.services

import com.medipath.core.models.CodeRequest
import retrofit2.Response
import retrofit2.http.*

interface CodesService {
    @PUT("/api/visits/code")
    suspend fun markCodeAsUsed(@Body request: CodeRequest): Response<Unit>

    @HTTP(method = "DELETE", path = "/api/visits/code", hasBody = true)
    suspend fun deleteCode(@Body request: CodeRequest): Response<Unit>
}

