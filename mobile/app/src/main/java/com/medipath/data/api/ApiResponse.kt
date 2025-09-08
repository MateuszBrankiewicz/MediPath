package com.medipath.data.api

data class ApiResponse(
    val message: String,
    val fields: List<String>? = null
)