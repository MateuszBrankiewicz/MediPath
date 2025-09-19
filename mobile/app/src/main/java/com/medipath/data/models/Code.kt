package com.medipath.data.models

data class CodeItem(
    val codes: CodeData
)

data class CodeData(
    val codeType: String,
    val code: String
)
