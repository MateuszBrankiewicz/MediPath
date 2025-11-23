package com.medipath.modules.patient.booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.models.DoctorScheduleItem
import com.medipath.core.models.Patient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class RescheduleViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val searchService = RetrofitInstance.searchService
    private val visitsService = RetrofitInstance.visitsService

    private val _schedules = MutableStateFlow<List<DoctorScheduleItem>>(emptyList())
    val schedules: StateFlow<List<DoctorScheduleItem>> = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage: SharedFlow<String> = _successMessage.asSharedFlow()

    private val _rescheduleSuccess = MutableStateFlow(false)
    val rescheduleSuccess: StateFlow<Boolean> = _rescheduleSuccess.asStateFlow()

    private val _patientInfo = MutableStateFlow<Patient?>(null)
    val patientInfo: StateFlow<Patient?> = _patientInfo.asStateFlow()

    private val context = getApplication<Application>()

    fun loadVisitDetails(visitId: String) {
        viewModelScope.launch {
            try {
                val response = visitsService.getVisitDetails(visitId)
                if (response.isSuccessful) {
                    _patientInfo.value = response.body()?.visit?.patient
                } else {
                    _errorMessage.emit(context.getString(R.string.failed_to_load_visit_details))
                }
            } catch (_: IOException) {
                _errorMessage.emit(context.getString(R.string.error_connection))
            } catch (_: Exception) {
                _errorMessage.emit(context.getString(R.string.unknown_error))
            }
        }
    }

    fun loadSchedules(doctorId: String, institutionId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = searchService.getDoctorSchedules(doctorId, institutionId)
                if (response.isSuccessful) {
                    _schedules.value = response.body()?.schedules ?: emptyList()
                } else {
                    _errorMessage.emit(context.getString(R.string.failed_to_load_schedules))
                }
            } catch (_: IOException) {
                _errorMessage.emit(context.getString(R.string.error_connection))
            } catch (_: Exception) {
                _errorMessage.emit(context.getString(R.string.unknown_error))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rescheduleVisit(visitId: String, newScheduleId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = visitsService.rescheduleVisit(
                    visitId = visitId,
                    newScheduleId = newScheduleId
                )
                if (response.isSuccessful) {
                    _rescheduleSuccess.value = true
                    _successMessage.emit(context.getString(R.string.visit_rescheduled_successfully))
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> context.getString(R.string.error_bad_request)
                        403 -> context.getString(R.string.no_permission_visit)
                        404 -> context.getString(R.string.no_visits_found)
                        else -> context.getString(R.string.an_unknown_error_occurred_please_try_again_later)
                    }
                    _errorMessage.emit(errorMsg)
                }
            } catch (_: IOException) {
                _errorMessage.emit(context.getString(R.string.error_connection))
            } catch (_: Exception) {
                _errorMessage.emit(context.getString(R.string.unknown_error))
            } finally {
                _isLoading.value = false
            }
        }
    }
}