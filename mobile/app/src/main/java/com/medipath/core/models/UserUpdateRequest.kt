package com.medipath.core.models

data class UserUpdateRequest (
    val city: String,
    val name: String,
    val surname: String,
    val province: String,
    val postalCode: String,
    val number: String,
    val street: String,
    val phoneNumber: String,
    val pfpImage: String?
)