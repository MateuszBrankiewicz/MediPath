package com.medipath.modules.patient.codes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.CodeItem
import com.medipath.core.models.CodeRequest
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class CodesViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val codesService = RetrofitInstance.codesService
    private val userService  = RetrofitInstance.userService
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
    
    private val context = getApplication<Application>()

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
                        400 -> context.getString(R.string.invalid_code_type)
                        401 -> context.getString(R.string.error_session)
                        else -> context.getString(R.string.error_fetching_codes)
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
                    _successMessage.value = context.getString(R.string.code_marked_as_used)
                    onSuccess()
                } else {
                    _error.value = when (response.code()) {
                        401 -> context.getString(R.string.error_session)
                        404 -> context.getString(R.string.code_not_found)
                        else -> context.getString(R.string.error_mark_code_as_used)
                    }
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
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
                        401 -> context.getString(R.string.error_session)
                        404 -> context.getString(R.string.code_not_found)
                        else -> context.getString(R.string.error_delete_code)
                    }
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
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