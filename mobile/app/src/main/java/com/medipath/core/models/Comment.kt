package com.medipath.core.models

data class DoctorCommentsResponse(
    val comments: List<Comment>
)

data class Comment(
    val doctor: String,
    val createdAt: String,
    val doctorRating: Double,
    val author: String,
    val id: String,
    val content: String,
    val institution: String,
    val institutionRating: Double
)

data class InstitutionCommentsResponse(
    val comments: List<Comment>
)

data class CommentResponse(
    val comment: Comment
)
