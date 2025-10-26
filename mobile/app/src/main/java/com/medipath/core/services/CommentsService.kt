package com.medipath.core.services

import com.medipath.core.models.AddCommentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CommentsService {
    @POST("/api/comments/add")
    suspend fun addComment(@Body comment: AddCommentRequest, @Header("Cookie") cookie: String): Response<Unit>
}
