package com.medipath.core.responses

data class SearchResponse(
    val result: List<SearchResult>
)

data class SearchResult(
    val id: String,
    val name: String,
    val image: String,
    val rating: Double,
    val numOfRatings: Int,

    val surname: String? = null,
    val addresses: List<Address>? = null,
    val specialisations: List<String>? = null,
    val schedules: List<Schedule>? = null,

    val address: String? = null,
    val types: List<String>? = null,
    val isPublic: Boolean? = null
) {
    fun isDoctorResult(): Boolean = surname != null
    fun isInstitutionResult(): Boolean = address != null

    fun getDisplayAddress(): String {
        return when {
            isDoctorResult() -> addresses?.firstOrNull()?.second ?: ""
            isInstitutionResult() -> address ?: ""
            else -> ""
        }
    }

    fun getSpecialisationsDisplay(): String {
        return when {
            isDoctorResult() -> specialisations?.joinToString(", ") ?: ""
            isInstitutionResult() -> types?.joinToString(", ") ?: ""
            else -> ""
        }
    }
}

data class Address(
    val first: String,
    val second: String
)

data class Schedule(
    val id: String,
    val isBooked: Boolean,
    val startTime: String
)