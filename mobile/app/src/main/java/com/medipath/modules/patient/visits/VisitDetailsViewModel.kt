package com.medipath.modules.patient.visits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.VisitDetails
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class VisitDetailsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val visitsService = RetrofitInstance.visitsService

    private val _visitDetails = MutableStateFlow<VisitDetails?>(null)
    val visitDetails: StateFlow<VisitDetails?> = _visitDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()
    
    private val context = getApplication<Application>()

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
                        403 -> context.getString(R.string.error_no_permission_to_view_visit)
                        404 -> context.getString(R.string.error_visit_not_found)
                        else -> context.getString(R.string.failed_to_load_visit_details)
                    }
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