package com.medipath.core.models

data class InstitutionDetailResponse(
    val institution: InstitutionDetail
)

data class InstitutionDetail(
    val id: String,
    val name: String,
    val types: List<String>? = null,
    val isPublic: Boolean,
    val address: Address,
    val employees: List<InstitutionEmployee>? = null,
    val rating: Double,
    val numOfRatings: Int = 0,
    val image: String? = null
)

data class InstitutionEmployee(
    val userId: String,
    val name: String,
    val surname: String,
    val specialisations: List<String>? = null,
    val roleCode: Int,
    val pfpimage: String
)
