package com.medipath.data.api

import com.medipath.data.models.City
import com.medipath.data.models.LoginRequest
import com.medipath.data.models.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("/api/cities/")
    suspend fun getCities(): List<City>

    @GET("/api/provinces")
    suspend fun getProvinces(): List<String>

    @POST("/api/users/register")
    suspend fun registerUser(@Body userData: RegisterRequest): ApiResponse

    @POST("/api/users/login")
    suspend fun loginUser(@Body loginData: LoginRequest): ApiResponse

    @GET("/api/users/resetpassword")
    suspend fun resetPassword(@Query("address") email: String): ApiResponse

    @POST("/api/users/logout")
    suspend fun logout(): ApiResponse

    @GET("/api/users/profile")
    suspend fun getUserProfile(): ApiResponse
}
