package com.medipath.core.models

data class InstitutionDoctorsResponse(
    val doctors: List<InstitutionDoctor>
)

data class InstitutionDoctor(
    val doctorPfp: String?,
    val doctorName: String,
    val doctorId: String,
    val licenceNumber: String?,
    val rating: Double,
    val doctorSurname: String?,
    val doctorSchedules: List<DoctorScheduleItem>?,
    val numofratings: Int
)
