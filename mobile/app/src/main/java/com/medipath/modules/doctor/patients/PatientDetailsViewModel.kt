package com.medipath.modules.doctor.patients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.PatientDetailsResponse
import com.medipath.core.models.PatientVisit
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.DoctorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okio.IOException

class PatientDetailsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val doctorService: DoctorService = RetrofitInstance.doctorService

    private val _patientDetails = MutableStateFlow<PatientDetailsResponse?>(null)
    val patientDetails: StateFlow<PatientDetailsResponse?> = _patientDetails

    private val _patientVisits = MutableStateFlow<List<PatientVisit>>(emptyList())
    val patientVisits: StateFlow<List<PatientVisit>> = _patientVisits

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val context = getApplication<Application>()
    
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
                } else {
                    _errorMessage.value = context.getString(R.string.failed_to_load_patient_details)
                }
            } catch (_: IOException) {
                _errorMessage.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _errorMessage.value = context.getString(R.string.unknown_error)
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
                    _patientVisits.value = visitsResponse?.visits
                        ?.sortedByDescending { it.startTime } ?: emptyList()
                } else {
                    _errorMessage.value = context.getString(R.string.failed_to_load_patient_visits)
                }
            } catch (_: IOException) {
                _errorMessage.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _errorMessage.value = context.getString(R.string.unknown_error)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
