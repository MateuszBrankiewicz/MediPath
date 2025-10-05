package com.medipath.core.models

data class User(
    val email: String,
    val name: String,
    val surname: String,
    val govId: String,
    val birthDate: List<Int>,
    val address: Address,
    val phoneNumber: String,
    val passwordHash: String,
    val userSettings: UserSettings,
    val id: String,
    val licenceNumber: String,
    val specialisations: List<String>,
    val latestMedicalHistory: List<MedicalHistory>,
    val roleCode: Int,
    val rating: Double,
    val employers: List<Employer> = emptyList(),
    val active: Boolean
)