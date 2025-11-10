package com.medipath.modules.shared.components

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun formatDate(dateString: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        dateString
    }
}