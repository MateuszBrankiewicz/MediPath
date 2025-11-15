package com.medipath.modules.doctor.patients

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.PatientDetailsResponse
import com.medipath.core.models.PatientVisit
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.DoctorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PatientDetailsViewModel : ViewModel() {
    private val doctorService: DoctorService = RetrofitInstance.doctorService

    private val _patientDetails = MutableStateFlow<PatientDetailsResponse?>(null)
    val patientDetails: StateFlow<PatientDetailsResponse?> = _patientDetails

    private val _patientVisits = MutableStateFlow<List<PatientVisit>>(emptyList())
    val patientVisits: StateFlow<List<PatientVisit>> = _patientVisits

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchPatientData(patientId: String) {
        fetchPatientDetails(patientId)
        fetchPatientVisits(patientId)
    }

    private fun fetchPatientDetails(patientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = doctorService.getPatientDetails(patientId)
                if (response.isSuccessful) {
                    _patientDetails.value = response.body()
                    Log.d("PatientDetailsViewModel", "Fetched patient details successfully")
                } else {
                    _errorMessage.value = "Error loading patient details: ${response.code()}"
                    Log.e("PatientDetailsViewModel", "Failed to fetch patient details: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
                Log.e("PatientDetailsViewModel", "Exception fetching patient details", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchPatientVisits(patientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = doctorService.getPatientVisits(patientId)
                if (response.isSuccessful) {
                    val visitsResponse = response.body()
                    // Sort visits by start time (most recent first)
                    _patientVisits.value = visitsResponse?.visits
                        ?.sortedByDescending { it.startTime } ?: emptyList()
                    Log.d("PatientDetailsViewModel", "Fetched ${_patientVisits.value.size} visits for patient")
                } else {
                    _errorMessage.value = "Error loading visits: ${response.code()}"
                    Log.e("PatientDetailsViewModel", "Failed to fetch visits: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
                Log.e("PatientDetailsViewModel", "Exception fetching visits", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
