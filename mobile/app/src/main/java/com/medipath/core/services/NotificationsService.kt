package com.medipath.core.services

import com.medipath.core.models.AddNotificationRequest
import com.medipath.core.models.DeleteNotificationsRequest
import com.medipath.core.models.MarkNotificationReadRequest
import com.medipath.core.models.NotificationsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.Query

interface NotificationsService {

    @GET("/api/users/me/notifications")
    suspend fun getUserNotifications(@Query("filter") filter: String): Response<NotificationsResponse>
    @POST("/api/notifications/read/")
    suspend fun markNotificationAsRead(@Body body: MarkNotificationReadRequest): Response<Unit>
    @POST("/api/notifications/readall/")
    suspend fun markAllNotificationsAsRead(): Response<Unit>
    @POST("/api/notifications/add")
    suspend fun addNotification(@Body body: AddNotificationRequest): Response<Unit>
    @HTTP( method = "DELETE", path = "/api/notifications/", hasBody = true)
    suspend fun deleteNotifications(@Body body: DeleteNotificationsRequest): Response<Unit>
}
