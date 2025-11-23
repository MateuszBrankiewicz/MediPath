package com.medipath.modules.doctor.dashboard

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import com.medipath.R
import com.medipath.core.models.Visit
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DoctorDashboardViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val doctorService = RetrofitInstance.doctorService


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedDateVisits = MutableStateFlow<List<Visit>>(emptyList())
    val selectedDateVisits: StateFlow<List<Visit>> = _selectedDateVisits.asStateFlow()

    private val _currentVisit = MutableStateFlow<Visit?>(null)
    val currentVisit: StateFlow<Visit?> = _currentVisit.asStateFlow()
    
    private val _selectedDatePatientCount = MutableStateFlow(0)
    val selectedDatePatientCount: StateFlow<Int> = _selectedDatePatientCount.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val context = getApplication<Application>()

    fun fetchVisitsForDate(date: LocalDate) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val dateString = if (date.isEqual(LocalDate.now())) {
                    "today"
                } else {
                    date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
                
                val response = doctorService.getDoctorVisitsByDate(dateString)
                
                if (response.isSuccessful) {
                    val visits = response.body()?.visits ?: emptyList()
                    
                    val activeVisits = visits.filter { it.status != "Cancelled" }
                    _selectedDateVisits.value = activeVisits
                    _selectedDatePatientCount.value = activeVisits.size
                    
                    if (date.isEqual(LocalDate.now())) {
                        _currentVisit.value = findCurrentVisit(activeVisits)
                    } else {
                        _currentVisit.value = null
                    }
                } else {
                    _error.value = context.getString(R.string.error_load_visits)
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

    private fun findCurrentVisit(visits: List<Visit>): Visit? {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        
        return visits.firstOrNull { visit ->
            if (visit.status == "Cancelled") return@firstOrNull false
            
            try {
                val startTime = LocalDateTime.parse(visit.time.startTime, formatter)
                val endTime = LocalDateTime.parse(visit.time.endTime, formatter)
                
                now.isAfter(startTime) && now.isBefore(endTime)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.error_data_format)
                false
            }
        }
    }
}
