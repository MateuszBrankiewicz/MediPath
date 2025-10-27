package com.medipath.core.models

data class AddNotificationRequest(
    val userId: String? = null,
    val content: String? = null,
    val title: String,
    val startDate: String,
    val endDate: String? = null,
    val reminderTime: String
)