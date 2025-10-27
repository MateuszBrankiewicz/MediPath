package com.medipath.core.models

import java.io.Serializable

data class Notification(
    val title: String,
    val content: String,
    val timestamp: String,
    val system: Boolean,
    val read: Boolean
) : Serializable