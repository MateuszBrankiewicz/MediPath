package com.medipath.core.models

import java.io.Serializable

data class Notification(
    val title: String,
    val content: String,
    val timestamp: List<Int>,
    val system: Boolean,
    val read: Boolean
) : Serializable