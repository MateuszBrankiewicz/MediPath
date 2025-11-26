package com.medipath.modules.patient.visits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.Visit
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class VisitsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val visitsService = RetrofitInstance.visitsService
    
    private val apiDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private val _visits = MutableStateFlow<List<Visit>>(emptyList())
    val visits: StateFlow<List<Visit>> = _visits.asStateFlow()

    private val _filteredVisits = MutableStateFlow<List<Visit>>(emptyList())
    val filteredVisits: StateFlow<List<Visit>> = _filteredVisits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _dateFromFilter = MutableStateFlow("")
    val dateFromFilter: StateFlow<String> = _dateFromFilter.asStateFlow()

    private val _dateToFilter = MutableStateFlow("")
    val dateToFilter: StateFlow<String> = _dateToFilter.asStateFlow()

    private val _sortBy = MutableStateFlow("Date")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow("Descending")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _totalVisits = MutableStateFlow(0)
    val totalVisits: StateFlow<Int> = _totalVisits.asStateFlow()

    private val _scheduledVisits = MutableStateFlow(0)
    val scheduledVisits: StateFlow<Int> = _scheduledVisits.asStateFlow()

    private val _completedVisits = MutableStateFlow(0)
    val completedVisits: StateFlow<Int> = _completedVisits.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    private val _cancelSuccess = MutableStateFlow(false)
    val cancelSuccess: StateFlow<Boolean> = _cancelSuccess.asStateFlow()

    private val context = getApplication<Application>()

    fun fetchVisits(upcoming: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _shouldRedirectToLogin.value = false

                val response = if (upcoming) {
                    visitsService.getUpcomingVisits("true")
                } else {
                    visitsService.getAllVisits()
                }

                if (response.isSuccessful) {
                    _visits.value = response.body()?.visits ?: emptyList()
                    updateStatistics()
                    applyFilters()
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
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

    fun cancelVisit(visitId: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                _cancelSuccess.value = false

                val response = visitsService.cancelVisit(visitId)

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
                    _cancelSuccess.value = true
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    val errorResId = when (response.code()) {
                        400 -> R.string.visit_already_completed
                        403 -> R.string.no_permission_visit
                        404 -> R.string.no_visits_found
                        else -> R.string.failed_to_cancel_visit
                    }
                    _error.value = context.getString(errorResId)
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
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
                    } catch (_: Exception) {
                        _error.value = context.getString(R.string.failed_to_parse_date)
                        true
                    }
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
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
                    } catch (_: Exception) {
                        _error.value = context.getString(R.string.failed_to_parse_date)
                        true
                    }
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
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