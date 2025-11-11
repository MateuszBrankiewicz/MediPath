package com.medipath.core.services

import com.medipath.core.models.UserSettingsRequest
import com.medipath.core.models.UserSettingsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface SettingsService {
    @GET("/api/users/me/settings")
    suspend fun getSettings(): Response<UserSettingsResponse>

    @PUT("/api/users/me/settings")
    suspend fun updateSettings(@Body settings: UserSettingsRequest): Response<Unit>
}