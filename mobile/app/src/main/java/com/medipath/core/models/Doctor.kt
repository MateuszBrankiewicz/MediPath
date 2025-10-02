package com.medipath.core.models

data class Doctor(
    val userId: String,
    val doctorName: String,
    val doctorSurname: String,
    val specialisations: List<String>,
    val valid: Boolean
)