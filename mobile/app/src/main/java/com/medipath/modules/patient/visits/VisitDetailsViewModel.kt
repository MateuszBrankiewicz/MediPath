package com.medipath.modules.patient.visits

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.VisitDetails
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.HttpException

class VisitDetailsViewModel : ViewModel() {
    private val visitsService = RetrofitInstance.visitsService

    private val _visitDetails = mutableStateOf<VisitDetails?>(null)
    val visitDetails: State<VisitDetails?> = _visitDetails

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchVisitDetails(visitId: String, sessionManager: DataStoreSessionManager) {
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

                val response = visitsService.getVisitDetails(
                    visitId = visitId,
                    cookie = "SESSION=$sessionId"
                )

                _visitDetails.value = response.visit
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("VisitDetailsViewModel", "Error fetching visit details", e)
                _error.value = when (e) {
                    is HttpException -> when (e.code()) {
                        401 -> "User is not logged in"
                        403 -> "You don't have permission to view this visit"
                        404 -> "Visit not found"
                        else -> "Failed to load visit details: ${e.message()}"
                    }
                    else -> "Failed to load visit details: ${e.message}"
                }
                _isLoading.value = false
            }
        }
    }
}