package com.medipath.core.models

data class DeleteNotificationsRequest(
    val title: String,
    val reminderTime: String,
    val startDate: String,
    val endDate: String
)
