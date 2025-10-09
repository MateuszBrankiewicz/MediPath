package com.medipath.core.models

data class Visit(
    val patient: Patient,
    val doctor: Doctor,
    val time: VisitTime,
    val institution: Institution,
    val id: String,
    val status: String,
    val note: String,
    val codes: List<Code>,
    val patientRemarks: String?
)

data class Code(
    val codeType: String,
    val code: String,
    val active: Boolean
)