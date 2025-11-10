package com.medipath.core.models

data class UpdateCommentRequest(
    val institutionRating: Double,
    val doctorRating: Double,
    val comment: String
)
