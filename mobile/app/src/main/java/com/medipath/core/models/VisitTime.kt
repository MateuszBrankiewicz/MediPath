package com.medipath.core.models

data class VisitTime(
    val scheduleId: String,
    val startTime: String,
    val endTime: String,
    val valid: Boolean
)