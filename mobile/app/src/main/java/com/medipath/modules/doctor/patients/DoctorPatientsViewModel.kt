package com.medipath.modules.doctor.patients

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.PatientDoc
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.DoctorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DoctorPatientsViewModel : ViewModel() {
    private val doctorService: DoctorService = RetrofitInstance.doctorService

    private val _patients = MutableStateFlow<List<PatientDoc>>(emptyList())
    val patients: StateFlow<List<PatientDoc>> = _patients

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

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
                        Log.d("DoctorPatientsViewModel", "Fetched ${sortedPatients.size} patients")
                    } else {
                        _errorMessage.value = "No data received"
                        Log.e("DoctorPatientsViewModel", "Response body is null")
                    }
                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                    Log.e("DoctorPatientsViewModel", "Failed to fetch patients: ${response.code()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
                Log.e("DoctorPatientsViewModel", "Exception fetching patients", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
