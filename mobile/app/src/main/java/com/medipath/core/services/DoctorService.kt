package com.medipath.core.services

import com.medipath.core.models.DoctorScheduleResponse
import com.medipath.core.models.PatientDetailsResponse
import com.medipath.core.models.PatientVisitsResponse
import com.medipath.core.models.PatientsResponse
import com.medipath.core.models.VisitsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DoctorService {
    @GET("/api/doctors/me/visits/{date}")
    suspend fun getDoctorVisitsByDate(@Path("date") date: String): Response<VisitsResponse>

    @GET("/api/doctors/me/visits")
    suspend fun getDoctorVisits(): Response<VisitsResponse>

    @GET("/api/doctors/me/patients")
    suspend fun getPatients(): Response<PatientsResponse>

    @GET("/api/doctors/me/patients/{patientId}/visits")
    suspend fun getPatientVisits(@Path("patientId") patientId: String): Response<PatientVisitsResponse>

    @GET("/api/users/patients/{patientId}")
    suspend fun getPatientDetails(@Path("patientId") patientId: String): Response<PatientDetailsResponse>

    @GET("/api/doctors/me/schedules")
    suspend fun getDoctorSchedules(): Response<DoctorScheduleResponse>
}
