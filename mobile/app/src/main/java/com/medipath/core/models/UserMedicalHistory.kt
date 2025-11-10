package com.medipath.core.models

data class UserMedicalHistory(
    val userId: String,
    val title: String,
    val note: String,
    val date: String,
    val doctor: Doctor?,
    val id: String
)

data class UserMedicalHistoryResponse(
    val medicalhistories: List<UserMedicalHistory>
)