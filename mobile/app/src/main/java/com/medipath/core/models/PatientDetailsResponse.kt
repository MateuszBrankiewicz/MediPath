package com.medipath.core.models

data class PatientDetailsResponse(
    val phoneNumber: String,
    val surname: String,
    val govId: String,
    val name: String,
    val pfp: String?,
    val birthDate: String,
    val medicalHistory: List<UserMedicalHistory>
)
