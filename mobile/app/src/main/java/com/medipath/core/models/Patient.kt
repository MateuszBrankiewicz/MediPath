package com.medipath.core.models

data class Patient(
    val userId: String,
    val name: String,
    val surname: String,
    val govID: String,
    val valid: Boolean
)