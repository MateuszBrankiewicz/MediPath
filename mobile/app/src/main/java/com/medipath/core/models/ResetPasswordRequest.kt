package com.medipath.core.models

data class ResetPasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
