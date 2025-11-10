package com.medipath.modules.patient.booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.BookingRequest
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.models.DoctorScheduleItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ToastMessage {
    data class Error(val message: String) : ToastMessage()
    data class Success(val message: String) : ToastMessage()
}

class BookingViewModel(application: Application) : AndroidViewModel(application) {
    private val searchService = RetrofitInstance.searchService
    private val visitsService = RetrofitInstance.visitsService

    private val _schedules = MutableStateFlow<List<DoctorScheduleItem>>(emptyList())
    val schedules: StateFlow<List<DoctorScheduleItem>> = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableSharedFlow<ToastMessage>()
    val toastMessage: SharedFlow<ToastMessage> = _toastMessage.asSharedFlow()

    private val _bookingSuccess = MutableStateFlow(false)
    val bookingSuccess: StateFlow<Boolean> = _bookingSuccess.asStateFlow()

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

    fun bookAppointment(scheduleId: String, notes: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val response = visitsService.bookAppointment(
                    BookingRequest(
                        scheduleID = scheduleId,
                        patientRemarks = notes
                    )
                )
                
                if (response.isSuccessful) {
                    _bookingSuccess.value = true
                    _toastMessage.emit(ToastMessage.Success("Appointment booked successfully!"))
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "This time slot is already booked"
                        404 -> "Schedule not found"
                        409 -> "You cannot book an appointment with yourself"
                        else -> "Failed to book appointment: ${response.code()}"
                    }
                    _toastMessage.emit(ToastMessage.Error(errorMessage))
                }
            } catch (e: Exception) {
                _toastMessage.emit(ToastMessage.Error("Error booking appointment: ${e.message}"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetBookingSuccess() {
        _bookingSuccess.value = false
    }
}
