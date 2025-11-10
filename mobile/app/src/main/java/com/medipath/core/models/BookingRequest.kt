package com.medipath.core.models

data class BookingRequest(
    val scheduleID: String,
    val patientRemarks: String? = null
)