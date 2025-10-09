package com.medipath.core.services

import com.medipath.core.models.City
import retrofit2.http.GET

interface LocationService {
    @GET("/api/cities/")
    suspend fun getCities(): List<City>

    @GET("/api/provinces")
    suspend fun getProvinces(): List<String>
}