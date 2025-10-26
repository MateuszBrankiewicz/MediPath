package com.medipath.core.models

data class AddCommentRequest(
    val visitID: String,
    val institutionRating: Double,
    val doctorRating: Double,
    val comment: String
)
