package com.medipath.core.models

data class CodeItem(
    val codes: CodeData,
    val date: String,
    val doctor: String
)

data class CodeData(
    val codeType: String,
    val code: String,
    val isActive: Boolean
)
