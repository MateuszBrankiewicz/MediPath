package com.medipath.modules.doctor.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.DoctorScheduleItem
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class DoctorScheduleViewModel(
    application: Application
) : AndroidViewModel(application){
    private val doctorService = RetrofitInstance.doctorService

    private val _schedules = MutableStateFlow<List<DoctorScheduleItem>>(emptyList())
    val schedules: StateFlow<List<DoctorScheduleItem>> = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()
    
    private val context = getApplication<Application>()

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
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                    _error.value = context.getString(R.string.error_session)
                } else {
                    _error.value = context.getString(R.string.failed_to_load_schedules)
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
