package com.medipath.core.models

data class UserSettingsRequest (
    val language: String,
    val systemNotifications: Boolean,
    val userNotifications: Boolean
)

data class UserSettingsResponse (
    val settings: UserSettings
)