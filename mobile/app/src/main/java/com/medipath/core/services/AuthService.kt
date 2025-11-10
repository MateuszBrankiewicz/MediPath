package com.medipath.core.services

import com.medipath.core.models.LoginRequest
import com.medipath.core.models.RegisterRequest
import com.medipath.core.models.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @POST("/api/users/register")
    suspend fun registerUser(@Body userData: RegisterRequest): Response<ApiResponse>

    @POST("/api/users/login")
    suspend fun loginUser(@Body loginData: LoginRequest): Response<ApiResponse>

    @GET("/api/users/resetpassword")
    suspend fun resetPassword(@Query("address") email: String): Response<ApiResponse>

    @POST("/api/users/logout")
    suspend fun logout(): Response<ApiResponse>
}