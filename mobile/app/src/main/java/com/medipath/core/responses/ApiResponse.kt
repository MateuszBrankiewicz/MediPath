package com.medipath.core.responses

data class ApiResponse(
    val message: String,
    val fields: List<String>? = null
)