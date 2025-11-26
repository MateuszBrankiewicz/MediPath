package com.medipath.modules.doctor.patients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.PatientDoc
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.DoctorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okio.IOException

class DoctorPatientsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val doctorService: DoctorService = RetrofitInstance.doctorService

    private val _patients = MutableStateFlow<List<PatientDoc>>(emptyList())
    val patients: StateFlow<List<PatientDoc>> = _patients

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val context = getApplication<Application>()

    fun fetchPatients() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = doctorService.getPatients()
                if (response.isSuccessful) {
                    val patientsResponse = response.body()
                    if (patientsResponse != null) {
                        val sortedPatients = patientsResponse.patients.sortedByDescending {
                            it.lastVisit.startTime
                        }
                        _patients.value = sortedPatients
                    } else {
                        _errorMessage.value = context.getString(R.string.error_no_data)
                    }
                } else {
                    _errorMessage.value = context.getString(R.string.failed_to_load_patients)
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
