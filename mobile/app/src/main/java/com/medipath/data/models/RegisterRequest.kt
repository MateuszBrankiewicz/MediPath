package com.medipath.data.models

data class RegisterRequest(
    val name: String,
    val surname: String,
    val email: String,
    val govID: String,
    val birthDate: String,
    val province: String,
    val city: String,
    val postalCode: String,
    val phoneNumber: String,
    val street: String,
    val number: String,
    val password: String
)