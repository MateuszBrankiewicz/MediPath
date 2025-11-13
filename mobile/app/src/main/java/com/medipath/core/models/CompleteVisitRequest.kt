package com.medipath.core.models

data class CompleteVisitRequest(
    val prescriptions: List<String>,
    val referrals: List<String>,
    val note: String
)