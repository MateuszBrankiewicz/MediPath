package com.medipath.core.models

data class UserSettings(
    val language: String,
    val systemNotifications: Boolean,
    val userNotifications: Boolean,
    val lastPanel: Int
)