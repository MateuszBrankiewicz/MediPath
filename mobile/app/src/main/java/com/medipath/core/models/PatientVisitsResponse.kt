package com.medipath.core.models

data class PatientVisitsResponse(
    val totalVisits: Int,
    val visits: List<PatientVisit>
)

data class PatientVisit(
    val note: String,
    val institution: String,
    val codes: List<Code>,
    val patientRemarks: String,
    val startTime: String,
    val id: String,
    val endTime: String,
    val status: String
)
