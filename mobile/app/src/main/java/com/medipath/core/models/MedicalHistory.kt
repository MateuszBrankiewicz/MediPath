package com.medipath.core.models

data class MedicalHistory(
    val userId: String?,
    val title: String,
    val note: String,
    val date: String,
    val doctor: Doctor?,
    val id: String
)