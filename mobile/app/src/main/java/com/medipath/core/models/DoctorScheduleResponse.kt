package com.medipath.core.models

data class DoctorScheduleResponse(
    val schedules: List<DoctorScheduleItem>
)

data class DoctorScheduleItem(
    val id: String,
    val startHour: String,
    val endHour: String,
    val booked: Boolean,
    val visitId: String?,
    val doctor: ScheduleDoctor,
    val institution: Institution
)

data class ScheduleDoctor(
    val userId: String,
    val doctorName: String,
    val doctorSurname: String,
    val specialisations: List<String>,
    val valid: Boolean
)
