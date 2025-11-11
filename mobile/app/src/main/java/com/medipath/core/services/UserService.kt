package com.medipath.core.services

import com.medipath.core.models.UserProfileResponse
import com.medipath.core.models.CodesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("/api/users/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @GET("/api/users/me/codes/{codeType}")
    suspend fun getUserCodes(@Path("codeType") codeType: String?): Response<CodesResponse>

    @GET("/api/users/me/codes")
    suspend fun getAllUserCodes(): Response<CodesResponse>
}
