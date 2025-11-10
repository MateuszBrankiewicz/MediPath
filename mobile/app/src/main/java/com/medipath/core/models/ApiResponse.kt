package com.medipath.core.models

data class ApiResponse(
    val message: String,
    val fields: List<String>? = null
)