package com.medipath.modules.patient.codes

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.CodeItem
import com.medipath.core.models.CodeRequest
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.CodesService
import com.medipath.core.services.UserService
import kotlinx.coroutines.launch

class CodesViewModel(
    private val codesService: CodesService = RetrofitInstance.codesService,
    private val userService: UserService = RetrofitInstance.userService
) : ViewModel() {

    private val _codes = mutableStateOf<List<CodeItem>>(emptyList())
    val codes: State<List<CodeItem>> = _codes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf("")
    val error: State<String> = _error

    private val _successMessage = mutableStateOf("")
    val successMessage: State<String> = _successMessage

    fun fetchCodes(sessionToken: String, codeType: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = ""

                val response = if (codeType != null) {
                    userService.getUserCodes(codeType, "SESSION=$sessionToken")
                } else {
                    userService.getAllUserCodes("SESSION=$sessionToken")
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

    fun markCodeAsUsed(sessionToken: String, codeType: String, code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _error.value = ""

                val request = CodeRequest(codeType = codeType, code = code)
                val response = codesService.markCodeAsUsed("SESSION=$sessionToken", request)

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

    fun deleteCode(sessionToken: String, codeType: String, code: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _error.value = ""

                val request = CodeRequest(codeType = codeType, code = code)
                val response = codesService.deleteCode( "SESSION=$sessionToken", request)

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