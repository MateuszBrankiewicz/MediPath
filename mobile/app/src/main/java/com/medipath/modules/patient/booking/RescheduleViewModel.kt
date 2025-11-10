package com.medipath.modules.patient.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.models.DoctorScheduleItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RescheduleViewModel : ViewModel() {
    private val searchService = RetrofitInstance.searchService
    private val visitsService = RetrofitInstance.visitsService

    private val _schedules = MutableStateFlow<List<DoctorScheduleItem>>(emptyList())
    val schedules: StateFlow<List<DoctorScheduleItem>> = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableSharedFlow<ToastMessage>()
    val toastMessage: SharedFlow<ToastMessage> = _toastMessage.asSharedFlow()

    private val _rescheduleSuccess = MutableStateFlow(false)
    val rescheduleSuccess: StateFlow<Boolean> = _rescheduleSuccess.asStateFlow()

    fun loadSchedules(doctorId: String, institutionId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val response = searchService.getDoctorSchedules(doctorId, institutionId)
                if (response.isSuccessful) {
                    _schedules.value = response.body()?.schedules ?: emptyList()
                } else {
                    _toastMessage.emit(ToastMessage.Error("Failed to load schedules: ${response.code()}"))
                }
            } catch (e: Exception) {
                _toastMessage.emit(ToastMessage.Error("Error loading schedules: ${e.message}"))
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
                    _toastMessage.emit(ToastMessage.Success("Visit rescheduled successfully!"))
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "This time slot is already booked or visit cannot be rescheduled"
                        403 -> "You don't have permission to reschedule this visit"
                        404 -> "Visit or schedule not found"
                        500 -> "Server error - invalid schedule data"
                        else -> "Failed to reschedule visit: ${response.code()}"
                    }
                    _toastMessage.emit(ToastMessage.Error(errorMessage))
                }
            } catch (e: Exception) {
                _toastMessage.emit(ToastMessage.Error("Error rescheduling visit: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetRescheduleSuccess() {
        _rescheduleSuccess.value = false
    }
}