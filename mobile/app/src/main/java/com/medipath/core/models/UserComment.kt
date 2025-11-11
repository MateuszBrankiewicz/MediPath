package com.medipath.core.models

data class UserComment(
    val id: String,
    val institution: String,
    val doctorRating: Double,
    val doctor: String,
    val content: String,
    val institutionRating: Double,
    val createdAt: String?
)

data class UserCommentsResponse(
    val comments: List<UserComment>
)
