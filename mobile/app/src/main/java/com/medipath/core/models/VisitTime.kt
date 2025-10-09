package com.medipath.core.models

data class VisitTime(
    val scheduleId: String,
    val startTime: List<Int>,
    val endTime: List<Int>,
    val valid: Boolean
)