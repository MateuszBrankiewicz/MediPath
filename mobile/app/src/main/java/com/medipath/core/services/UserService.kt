package com.medipath.core.services

import com.medipath.core.models.UserProfileResponse
import com.medipath.core.models.CodesResponse
import com.medipath.core.models.UserSettingsRequest
import com.medipath.core.models.UserUpdateRequest
import com.medipath.core.models.ResetPasswordRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

interface UserService {
    @GET("/api/users/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @GET("/api/users/me/codes/{codeType}")
    suspend fun getUserCodes(@Path("codeType") codeType: String?): Response<CodesResponse>

    @GET("/api/users/me/codes")
    suspend fun getAllUserCodes(): Response<CodesResponse>

    @PUT("/api/users/me/update")
    suspend fun updateProfile(@Body profile: UserUpdateRequest): Response<Unit>

    @POST("/api/users/me/resetpassword")
    suspend fun resetPassword(@Body password: ResetPasswordRequest): Response<Unit>
}
