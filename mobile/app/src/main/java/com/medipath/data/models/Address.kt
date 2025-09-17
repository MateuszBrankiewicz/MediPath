package com.medipath.data.models

data class Address(
    val province: String,
    val city: String,
    val street: String,
    val number: String,
    val postalCode: String,
    val valid: Boolean
)
