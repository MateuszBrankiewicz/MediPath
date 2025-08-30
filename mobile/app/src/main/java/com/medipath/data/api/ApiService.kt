package com.medipath.data.api

import com.medipath.data.models.City
import retrofit2.http.GET

interface ApiService {
    @GET("/api/cities/")
    suspend fun getCities(): List<City>
}
