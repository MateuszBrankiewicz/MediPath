package com.medipath.core.models

data class NotificationsResponse(
    val user: List<Notification>
)

data class Notification(
    val title: String,
    val content: String,
    val timestamp: List<Int>,
    val system: Boolean,
    val read: Boolean
)