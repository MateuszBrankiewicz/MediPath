package com.medipath.modules.doctor.visit

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import com.medipath.R
import com.medipath.core.models.CompleteVisitRequest
import com.medipath.core.models.UserMedicalHistory
import com.medipath.core.models.Visit
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException

class DoctorVisitViewModel(
    application: Application
) : AndroidViewModel(application){
    private val medicalHistoryService = RetrofitInstance.medicalHistoryService
    private val visitsService = RetrofitInstance.visitsService

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory.asStateFlow()

    private val _visit = MutableStateFlow<Visit?>(null)
    val visit: StateFlow<Visit?> = _visit.asStateFlow()

    private val _medicalHistory = MutableStateFlow<List<UserMedicalHistory>>(emptyList())
    val medicalHistory: StateFlow<List<UserMedicalHistory>> = _medicalHistory.asStateFlow()

    private val _isCurrentVisit = MutableStateFlow(false)
    val isCurrentVisit: StateFlow<Boolean> = _isCurrentVisit.asStateFlow()

    private val _prescriptionCodes = MutableStateFlow("")
    val prescriptionCodes: StateFlow<String> = _prescriptionCodes.asStateFlow()

    private val _referralCodes = MutableStateFlow("")
    val referralCodes: StateFlow<String> = _referralCodes.asStateFlow()

    private val _noteText = MutableStateFlow("")
    val noteText: StateFlow<String> = _noteText.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _visitCompleted = MutableStateFlow(false)
    val visitCompleted: StateFlow<Boolean> = _visitCompleted.asStateFlow()

    private val context = getApplication<Application>()

    fun setVisit(visit: Visit, isCurrent: Boolean) {
        _visit.value = visit
        _isCurrentVisit.value = isCurrent
        fetchPatientMedicalHistory(visit.patient.userId)
        
        if (visit.status == "Completed") {
            _noteText.value = visit.note

            val prescriptions = visit.codes.filter { it.codeType.equals("PRESCRIPTION", ignoreCase = true) }
                .map { it.code }
            val referrals = visit.codes.filter { it.codeType.equals("REFERRAL", ignoreCase = true) }
                .map { it.code }
            
            _prescriptionCodes.value = prescriptions.joinToString(", ")
            _referralCodes.value = referrals.joinToString(", ")
        }
    }

    private fun fetchPatientMedicalHistory(patientId: String) {
        viewModelScope.launch {
            _isLoadingHistory.value = true
            _error.value = null
            try {
                val response = medicalHistoryService.getPatientMedicalHistory(patientId)
                if (response.isSuccessful) {
                    _medicalHistory.value = response.body()?.medicalhistories ?: emptyList()
                } else {
                    _error.value = context.getString(R.string.failed_to_load_medical_history)
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
            } finally {
                _isLoadingHistory.value = false
            }
        }
    }

    fun setPrescriptionCodes(text: String) {
        _prescriptionCodes.value = text
    }

    fun setReferralCodes(text: String) {
        _referralCodes.value = text
    }

    fun setNoteText(text: String) {
        _noteText.value = text
    }

    fun completeVisit() {
        val visitId = _visit.value?.id ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val prescriptions = _prescriptionCodes.value
                    .split(",", "\n", " ")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                
                val referrals = _referralCodes.value
                    .split(",", "\n", " ")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                
                val request = CompleteVisitRequest(
                    prescriptions = prescriptions,
                    referrals = referrals,
                    note = _noteText.value
                )
                
                val response = visitsService.completeVisit(visitId, request)
                
                if (response.isSuccessful) {
                    _visitCompleted.value = true
                } else {
                    _error.value = when (response.code()) {
                        400 -> context.getString(R.string.visit_is_already_completed_or_cancelled)
                        403 -> context.getString(R.string.no_permission_visit)
                        401 -> context.getString(R.string.please_log_in_again)
                        else -> context.getString(R.string.failed_to_complete_visit)
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
