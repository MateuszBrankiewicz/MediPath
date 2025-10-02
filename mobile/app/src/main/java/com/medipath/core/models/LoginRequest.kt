package com.medipath.core.models

data class LoginRequest(
    val email: String,
    val password: String
)