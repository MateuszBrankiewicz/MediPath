package com.medipath.modules.patient.booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
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
import okio.IOException

class BookingViewModel(application: Application) : AndroidViewModel(application) {
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

    private val _bookingSuccess = MutableStateFlow(false)
    val bookingSuccess: StateFlow<Boolean> = _bookingSuccess.asStateFlow()

    private val context = getApplication<Application>()

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
                    _successMessage.emit(context.getString(R.string.appointment_booked_successfully))
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> context.getString(R.string.this_time_slot_is_already_booked)
                        404 -> context.getString(R.string.schedule_not_found)
                        409 -> context.getString(R.string.you_cannot_book_an_appointment_with_yourself)
                        else -> context.getString(R.string.failed_to_book_appointment)
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
