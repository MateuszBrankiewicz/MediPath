package com.medipath.modules.patient.visits

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.Visit
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class VisitsViewModel : ViewModel() {
    private val visitsService = RetrofitInstance.visitsService
    
    private val apiDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private val _visits = mutableStateOf<List<Visit>>(emptyList())
    val visits: State<List<Visit>> = _visits

    private val _filteredVisits = mutableStateOf<List<Visit>>(emptyList())
    val filteredVisits: State<List<Visit>> = _filteredVisits

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _statusFilter = mutableStateOf("All")
    val statusFilter: State<String> = _statusFilter

    private val _dateFromFilter = mutableStateOf("")
    val dateFromFilter: State<String> = _dateFromFilter

    private val _dateToFilter = mutableStateOf("")
    val dateToFilter: State<String> = _dateToFilter

    private val _sortBy = mutableStateOf("Date")
    val sortBy: State<String> = _sortBy

    private val _sortOrder = mutableStateOf("Descending")
    val sortOrder: State<String> = _sortOrder

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _totalVisits = mutableStateOf(0)
    val totalVisits: State<Int> = _totalVisits

    private val _scheduledVisits = mutableStateOf(0)
    val scheduledVisits: State<Int> = _scheduledVisits

    private val _completedVisits = mutableIntStateOf(0)
    val completedVisits: State<Int> = _completedVisits

    fun fetchVisits(sessionManager: DataStoreSessionManager, upcoming: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val sessionId = sessionManager.getSessionId()
                if (sessionId == null) {
                    _error.value = "No session found"
                    _isLoading.value = false
                    return@launch
                }

                val response = visitsService.getAllVisits(
                    cookie = "SESSION=$sessionId"
                )

                _visits.value = response.visits
                updateStatistics()
                applyFilters()
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("VisitsViewModel", "Error fetching visits", e)
                _error.value = "Failed to load visits: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun cancelVisit(visitId: String, sessionManager: DataStoreSessionManager) {
        viewModelScope.launch {
            try {
                val sessionId = sessionManager.getSessionId()
                if (sessionId == null) {
                    _error.value = "No session found"
                    return@launch
                }

                val response = visitsService.cancelVisit(visitId, "SESSION=$sessionId")
                
                if (response.isSuccessful) {
                    _visits.value = _visits.value.map {
                        if (it.id == visitId) {
                            it.copy(status = "cancelled")
                        } else {
                            it
                        }
                    }
                    updateStatistics()
                    applyFilters()
                } else {
                    _error.value = "Failed to cancel visit"
                }
            } catch (e: Exception) {
                _error.value = "Failed to cancel visit: ${e.message}"
            }
        }
    }

    fun updateStatusFilter(status: String) {
        _statusFilter.value = status
        applyFilters()
    }

    fun updateDateFromFilter(date: String) {
        _dateFromFilter.value = date
        applyFilters()
    }

    fun updateDateToFilter(date: String) {
        _dateToFilter.value = date
        applyFilters()
    }

    fun updateSortBy(sort: String) {
        _sortBy.value = sort
        applyFilters()
    }

    fun updateSortOrder(order: String) {
        _sortOrder.value = order
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun clearFilters() {
        _statusFilter.value = "All"
        _dateFromFilter.value = ""
        _dateToFilter.value = ""
        _sortBy.value = "Date"
        _sortOrder.value = "Descending"
        _searchQuery.value = ""
        applyFilters()
    }

    private fun updateStatistics() {
        _totalVisits.value = _visits.value.size
        _scheduledVisits.value = _visits.value.count { 
            it.status.equals("upcoming", ignoreCase = true) || it.status.equals("scheduled", ignoreCase = true) 
        }
        _completedVisits.value = _visits.value.count { it.status.equals("completed", ignoreCase = true) }
    }

    private fun applyFilters() {
        var filtered = _visits.value

        filtered = when (_statusFilter.value) {
            "Scheduled" -> filtered.filter { 
                it.status.equals("upcoming", ignoreCase = true) || it.status.equals("scheduled", ignoreCase = true) 
            }
            "Completed" -> filtered.filter { it.status.equals("completed", ignoreCase = true) }
            "Cancelled" -> filtered.filter { it.status.equals("cancelled", ignoreCase = true) }
            else -> filtered
        }

        if (_dateFromFilter.value.isNotEmpty()) {
            try {
                val fromDate = LocalDate.parse(_dateFromFilter.value, DateTimeFormatter.ISO_LOCAL_DATE)
                filtered = filtered.filter { visit ->
                    try {
                        val visitDate = LocalDateTime.parse(
                            visit.time.startTime,
                            apiDateTimeFormatter
                        ).toLocalDate()
                        !visitDate.isBefore(fromDate)
                    } catch (e: Exception) {
                        Log.e("VisitsViewModel", "Error parsing date: ${visit.time.startTime}", e)
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e("VisitsViewModel", "Error parsing filter date from: ${_dateFromFilter.value}", e)
            }
        }

        if (_dateToFilter.value.isNotEmpty()) {
            try {
                val toDate = LocalDate.parse(_dateToFilter.value, DateTimeFormatter.ISO_LOCAL_DATE)
                filtered = filtered.filter { visit ->
                    try {
                        val visitDate = LocalDateTime.parse(
                            visit.time.startTime,
                            apiDateTimeFormatter
                        ).toLocalDate()
                        !visitDate.isAfter(toDate)
                    } catch (e: Exception) {
                        Log.e("VisitsViewModel", "Error parsing date: ${visit.time.startTime}", e)
                        true
                    }
                }
            } catch (e: Exception) {
                Log.e("VisitsViewModel", "Error parsing filter date to: ${_dateToFilter.value}", e)
            }
        }

        if (_searchQuery.value.isNotEmpty()) {
            val query = _searchQuery.value.lowercase()
            filtered = filtered.filter {
                it.doctor.doctorName.lowercase().contains(query) ||
                it.doctor.doctorSurname.lowercase().contains(query) ||
                it.institution.institutionName.lowercase().contains(query) ||
                it.note.lowercase().contains(query)
            }
        }

        filtered = when (_sortBy.value) {
            "Date" -> filtered.sortedBy { it.time.startTime }
            "Doctor" -> filtered.sortedBy { "${it.doctor.doctorSurname} ${it.doctor.doctorName}" }
            "Institution" -> filtered.sortedBy { it.institution.institutionName }
            else -> filtered
        }

        if (_sortOrder.value == "Descending") {
            filtered = filtered.reversed()
        }

        _filteredVisits.value = filtered
    }
}