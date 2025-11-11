package com.medipath.modules.patient.visits

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.VisitDetails
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VisitDetailsViewModel : ViewModel() {
    private val visitsService = RetrofitInstance.visitsService

    private val _visitDetails = MutableStateFlow<VisitDetails?>(null)
    val visitDetails: StateFlow<VisitDetails?> = _visitDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    fun fetchVisitDetails(visitId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _shouldRedirectToLogin.value = false

                val response = visitsService.getVisitDetails(visitId = visitId)

                if (response.isSuccessful) {
                    _visitDetails.value = response.body()?.visit
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = when (response.code()) {
                        403 -> "You don't have permission to view this visit"
                        404 -> "Visit not found"
                        else -> "Failed to load visit details: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e("VisitDetailsViewModel", "Error fetching visit details", e)
                if (e is HttpException && e.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = "Failed to load visit details: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}