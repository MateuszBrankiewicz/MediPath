package com.medipath.core.services

import com.medipath.core.models.AddCommentRequest
import com.medipath.core.models.CommentResponse
import com.medipath.core.models.InstitutionCommentsResponse
import com.medipath.core.models.UpdateCommentRequest
import com.medipath.core.models.UserCommentsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Path

interface CommentsService {
    @POST("/api/comments/add")
    suspend fun addComment(@Body comment: AddCommentRequest): Response<Unit>

    @GET("/api/comments/institution/{id}")
    suspend fun getInstitutionComments(@Path("id") institutionId: String): Response<InstitutionCommentsResponse>

    @GET("/api/users/me/comments")
    suspend fun getUserComments(): Response<UserCommentsResponse>

    @GET("/api/comments/{id}")
    suspend fun getComment(@Path("id") commentId: String): Response<CommentResponse>

    @HTTP(method = "DELETE", path = "/api/comments/{id}")
    suspend fun deleteComment(@Path("id") commentId: String): Response<Unit>

    @PUT("/api/comments/{id}")
    suspend fun updateComment(
        @Path("id") commentId: String,
        @Body comment: UpdateCommentRequest
    ): Response<Unit>
}
