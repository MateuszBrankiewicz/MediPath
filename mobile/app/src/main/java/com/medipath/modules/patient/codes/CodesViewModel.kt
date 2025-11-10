package com.medipath.modules.patient.codes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.CodeItem
import com.medipath.core.models.CodeRequest
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.CodesService
import com.medipath.core.services.UserService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CodesViewModel(
    private val codesService: CodesService = RetrofitInstance.codesService,
    private val userService: UserService = RetrofitInstance.userService
) : ViewModel() {

    private val _codes = MutableStateFlow<List<CodeItem>>(emptyList())
    val codes: StateFlow<List<CodeItem>> = _codes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    fun fetchCodes(codeType: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = ""

                val response = if (codeType != null) {
                    userService.getUserCodes(codeType)
                } else {
                    userService.getAllUserCodes()
                }

                if (response.isSuccessful) {
                    _codes.value = response.body()?.codes ?: emptyList()
                } else {
                    _error.value = when (response.code()) {
                        400 -> "Invalid code type"
                        401 -> "Session expired, please log in again"
                        else -> "Error fetching codes (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markCodeAsUsed(codeType: String, code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _error.value = ""

                val request = CodeRequest(codeType = codeType, code = code)
                val response = codesService.markCodeAsUsed(request)

                if (response.isSuccessful) {
                    _codes.value = _codes.value.map { codeItem ->
                        if (codeItem.codes.code == code && codeItem.codes.codeType == codeType) {
                            codeItem.copy(
                                codes = codeItem.codes.copy(isActive = false)
                            )
                        } else {
                            codeItem
                        }
                    }
                    _successMessage.value = "Code marked as used"
                    onSuccess()
                } else {
                    _error.value = when (response.code()) {
                        401 -> "Session expired, please log in again"
                        404 -> "Code not found"
                        else -> "Error marking code as used (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun deleteCode(codeType: String, code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _error.value = ""

                val request = CodeRequest(codeType = codeType, code = code)
                val response = codesService.deleteCode(request)

                if (response.isSuccessful) {
                    _codes.value = _codes.value.filter {
                        !(it.codes.code == code && it.codes.codeType == codeType)
                    }
                    _successMessage.value = "Code deleted"
                    onSuccess()
                } else {
                    _error.value = when (response.code()) {
                        401 -> "Session expired, please log in again"
                        404 -> "Code not found"
                        else -> "Error deleting code (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = ""
    }

    fun clearSuccessMessage() {
        _successMessage.value = ""
    }
}