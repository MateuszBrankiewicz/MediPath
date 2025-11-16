package com.medipath.modules.doctor.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.DoctorScheduleItem
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DoctorScheduleViewModel : ViewModel() {
    private val doctorService = RetrofitInstance.doctorService

    private val _schedules = MutableStateFlow<List<DoctorScheduleItem>>(emptyList())
    val schedules: StateFlow<List<DoctorScheduleItem>> = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    init {
        fetchSchedules()
    }

    fun fetchSchedules() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _shouldRedirectToLogin.value = false

                val response = doctorService.getDoctorSchedules()

                if (response.isSuccessful) {
                    _schedules.value = response.body()?.schedules ?: emptyList()
                    Log.d("DoctorScheduleViewModel", "Loaded ${_schedules.value.size} schedules")
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                    _error.value = "Session expired. Please log in again."
                } else {
                    _error.value = "Failed to load schedules: ${response.code()}"
                    Log.e("DoctorScheduleViewModel", "Error loading schedules: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.value = "Error loading schedules: ${e.message}"
                Log.e("DoctorScheduleViewModel", "Exception loading schedules", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
