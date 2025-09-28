package com.medipath.data.models

data class VisitTime(
    val scheduleId: String,
    val startTime: List<Int>,
    val endTime: List<Int>,
    val valid: Boolean
)