package com.medipath.core.services

import com.medipath.core.models.AddNotificationRequest
import com.medipath.core.models.DeleteNotificationsRequest
import com.medipath.core.models.MarkNotificationReadRequest
import com.medipath.core.models.NotificationsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST

interface NotificationsService {

    @GET("/api/users/me/notifications")
    suspend fun getUserNotifications(@Header("Cookie") cookie: String): NotificationsResponse
    @POST("/api/notifications/read/")
    suspend fun markNotificationAsRead(@Body body: MarkNotificationReadRequest, @Header("Cookie") cookie: String): Response<Unit>
    @POST("/api/notifications/readall/")
    suspend fun markAllNotificationsAsRead(@Header("Cookie") cookie: String): Response<Unit>
    @POST("/api/notifications/add")
    suspend fun addNotification(@Body body: AddNotificationRequest, @Header("Cookie") cookie: String): Response<Unit>
    @HTTP( method = "DELETE", path = "/api/users/me/notifications", hasBody = true)
    suspend fun deleteNotifications(@Body body: DeleteNotificationsRequest, @Header("Cookie") cookie: String): Response<Unit>
}
