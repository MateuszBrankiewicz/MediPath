package com.medipath.data.api

import com.medipath.data.models.City
import com.medipath.data.models.LoginRequest
import com.medipath.data.models.RegisterRequest
import com.medipath.data.models.UserProfileResponse
import com.medipath.data.models.VisitsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/cities/")
    suspend fun getCities(): List<City>

    @GET("/api/provinces")
    suspend fun getProvinces(): List<String>

    @POST("/api/users/register")
    suspend fun registerUser(@Body userData: RegisterRequest): Response<ApiResponse>

    @POST("/api/users/login")
    suspend fun loginUser(@Body loginData: LoginRequest): Response<ApiResponse>

    @GET("/api/users/resetpassword")
    suspend fun resetPassword(@Query("address") email: String): Response<ApiResponse>

    @POST("/api/users/logout")
    suspend fun logout(): Response<ApiResponse>

    @GET("/api/users/profile")
    suspend fun getUserProfile(@Header("Cookie") cookie: String): UserProfileResponse

    @GET("/api/visits/getupcoming/{userid}")
    suspend fun getUpcomingVisits(@Path("userid") userId: String, @Header("Cookie") cookie: String): VisitsResponse

    @DELETE("/api/visits/{visitid}")
    suspend fun cancelVisit(@Path("visitid") visitId: String, @Header("Cookie") cookie: String): Response<Unit>

    @GET("/api/search/{query}")
    suspend fun search(@Path("query") query: String, @Query("type") type: String, @Query("city") city: String? = null, @Query("specialisation") specialisation: String? = null): Response<SearchResponse>

    @GET("api/visits/getactivecodes/{userid}")
    suspend fun getActiveCodes(@Path("userid") userId: String): Response<CodesResponse>
}
