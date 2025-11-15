package com.medipath.core.models

data class PatientsResponse(
    val patients: List<PatientDoc>
)

data class PatientDoc(
    val id: String,
    val name: String,
    val surname: String,
    val lastVisit: VisitDoc
)

data class VisitDoc(
    val id: String,
    val startTime: String,
    val endTime: String,
    val status: String
)
