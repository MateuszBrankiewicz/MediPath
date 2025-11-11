package com.medipath.core.models

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
    val addresses: List<Pair<Institution, String>>? = null,
    val specialisations: List<String>? = null,
    val schedules: List<Schedule>? = null,

    val address: String? = null,
    val types: List<String>? = null,
    val isPublic: Boolean? = null
)
data class Schedule(
    val id: String,
    val isBooked: Boolean,
    val startTime: String,
    val institution: Institution? = null
)