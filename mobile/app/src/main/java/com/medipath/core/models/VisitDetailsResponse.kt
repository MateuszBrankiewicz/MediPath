package com.medipath.core.models

data class VisitDetailsResponse(
    val visit: VisitDetails
)

data class VisitDetails(
    val patient: Patient,
    val doctor: Doctor,
    val time: VisitTime,
    val institution: Institution,
    val patientRemarks: String,
    val id: String,
    val status: String,
    val note: String,
    val codes: List<Code>,
    val commentId: String?
)
