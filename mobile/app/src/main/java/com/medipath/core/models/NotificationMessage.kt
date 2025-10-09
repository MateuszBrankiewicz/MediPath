package com.medipath.core.models

data class NotificationMessage(
    val title: String,
    val content: String,
    val type: String? = null,
    val timestamp: String? = null
)
