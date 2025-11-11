package com.medipath.core.services

import com.medipath.core.models.City
import retrofit2.Response
import retrofit2.http.GET

interface LocationService {
    @GET("/api/cities/")
    suspend fun getCities(): Response<List<City>>

    @GET("/api/provinces")
    suspend fun getProvinces(): Response<List<String>>
}