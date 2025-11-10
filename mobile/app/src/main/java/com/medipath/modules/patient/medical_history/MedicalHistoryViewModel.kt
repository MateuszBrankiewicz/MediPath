package com.medipath.modules.patient.medical_history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.MedicalHistoryRequest
import com.medipath.core.models.UserMedicalHistory
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedicalHistoryViewModel : ViewModel() {
    private val medicalHistoryService = RetrofitInstance.medicalHistoryService
    private val userService = RetrofitInstance.userService

    private val _medicalHistories = MutableStateFlow<List<UserMedicalHistory>>(emptyList())
    val medicalHistories: StateFlow<List<UserMedicalHistory>> = _medicalHistories.asStateFlow()

    private val _filteredMedicalHistories = MutableStateFlow<List<UserMedicalHistory>>(emptyList())
    val filteredMedicalHistories: StateFlow<List<UserMedicalHistory>> = _filteredMedicalHistories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow("Date")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow("Descending")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _totalHistories = MutableStateFlow(0)
    val totalHistories: StateFlow<Int> = _totalHistories.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    private val _addUpdateSuccess = MutableStateFlow(false)
    val addUpdateSuccess: StateFlow<Boolean> = _addUpdateSuccess.asStateFlow()

    fun fetchMedicalHistories() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _shouldRedirectToLogin.value = false

                val response = medicalHistoryService.getUserMedicalHistory()

                if (response.isSuccessful) {
                    _medicalHistories.value = response.body()?.medicalhistories ?: emptyList()
                    _totalHistories.value = _medicalHistories.value.size
                    applyFilters()
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = "Failed to load medical history: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("MedicalHistoryViewModel", "Error fetching medical histories", e)
                _error.value = "Failed to load medical history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMedicalHistory(historyId: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                _deleteSuccess.value = false

                val response = medicalHistoryService.deleteMedicalHistory(historyId)

                if (response.isSuccessful) {
                    _medicalHistories.value = _medicalHistories.value.filter { it.id != historyId }
                    _totalHistories.value = _medicalHistories.value.size
                    applyFilters()
                    _deleteSuccess.value = true
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = "Failed to delete medical history: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete medical history: ${e.message}"
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
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

    fun clearFilters() {
        _searchQuery.value = ""
        _sortBy.value = "Date"
        _sortOrder.value = "Descending"
        applyFilters()
    }

    fun addMedicalHistory(title: String, note: String, date: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _addUpdateSuccess.value = false

                val request = MedicalHistoryRequest(
                    title = title,
                    note = note,
                    date = date
                )

                Log.d("MedicalHistoryViewModel", "Adding medical history with request: $request")
                val response = medicalHistoryService.addMedicalHistory(request)

                if (response.isSuccessful) {
                    _addUpdateSuccess.value = true
                    fetchMedicalHistories()
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = "Failed to add medical history: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("MedicalHistoryViewModel", "Error adding medical history", e)
                _error.value = "Failed to add medical history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMedicalHistory(historyId: String, title: String, note: String, date: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _addUpdateSuccess.value = false

                val request = MedicalHistoryRequest(
                    title = title,
                    note = note,
                    date = date
                )

                Log.d("MedicalHistoryViewModel", "Updating medical history with request: $request")
                val response = medicalHistoryService.updateMedicalHistory(historyId, request)

                if (response.isSuccessful) {
                    _addUpdateSuccess.value = true
                    fetchMedicalHistories()
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = "Failed to update medical history: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("MedicalHistoryViewModel", "Error updating medical history", e)
                _error.value = "Failed to update medical history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getMedicalHistoryById(historyId: String): UserMedicalHistory? {
        return _medicalHistories.value.find { it.id == historyId }
    }

    private fun applyFilters() {
        var filtered = _medicalHistories.value

        if (_searchQuery.value.isNotEmpty()) {
            val query = _searchQuery.value.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(query) ||
                it.note.lowercase().contains(query) ||
                it.doctor?.let { doctor -> 
                    "${doctor.doctorName} ${doctor.doctorSurname}".lowercase().contains(query)
                } ?: false
            }
        }

        filtered = when (_sortBy.value) {
            "Date" -> filtered.sortedBy { it.date }
            "Title" -> filtered.sortedBy { it.title }
            "Doctor" -> filtered.sortedBy { 
                it.doctor?.let { doctor -> "${doctor.doctorName} ${doctor.doctorSurname}" } ?: ""
            }
            else -> filtered.sortedBy { it.date }
        }

        if (_sortOrder.value == "Descending") {
            filtered = filtered.reversed()
        }

        _filteredMedicalHistories.value = filtered
    }
}
