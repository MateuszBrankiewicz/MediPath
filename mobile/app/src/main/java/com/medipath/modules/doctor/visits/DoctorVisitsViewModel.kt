package com.medipath.modules.doctor.visits

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

class DoctorVisitsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val doctorService = RetrofitInstance.doctorService

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _allVisits = MutableStateFlow<List<Visit>>(emptyList())
    
    private val _filteredVisits = MutableStateFlow<List<Visit>>(emptyList())
    val filteredVisits: StateFlow<List<Visit>> = _filteredVisits.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow("Ascending")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _totalVisits = MutableStateFlow(0)
    val totalVisits: StateFlow<Int> = _totalVisits.asStateFlow()

    private val context = getApplication<Application>()

    fun fetchVisits() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = doctorService.getDoctorVisits()
                
                if (response.isSuccessful) {
                    val visits = response.body()?.visits ?: emptyList()
                    _allVisits.value = visits
                    _totalVisits.value = visits.size
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updateStatusFilter(status: String) {
        _statusFilter.value = status
        applyFilters()
    }

    fun updateSortOrder(order: String) {
        _sortOrder.value = order
        applyFilters()
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _statusFilter.value = "All"
        _sortOrder.value = "Descending"
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = _allVisits.value

        if (_statusFilter.value != "All") {
            filtered = filtered.filter { it.status == _statusFilter.value }
        }

        if (_searchQuery.value.isNotBlank()) {
            val query = _searchQuery.value.lowercase()
            filtered = filtered.filter {
                it.patient.name.lowercase().contains(query) ||
                it.patient.surname.lowercase().contains(query) ||
                it.patient.govID.lowercase().contains(query)
            }
        }

        filtered = if (_sortOrder.value == "Ascending") {
            filtered.sortedBy { it.time.startTime }
        } else {
            filtered.sortedByDescending { it.time.startTime }
        }

        _filteredVisits.value = filtered
    }
}
