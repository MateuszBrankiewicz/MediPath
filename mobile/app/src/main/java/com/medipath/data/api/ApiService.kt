package com.medipath.data.api

import com.medipath.data.models.City
import com.medipath.data.models.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/api/cities/")
    suspend fun getCities(): List<City>

    @GET("/api/provinces")
    suspend fun getProvinces(): List<String>

    @POST("/api/users/register")
    suspend fun registerUser(@Body userData: RegisterRequest): ApiResponse
}
