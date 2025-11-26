package com.medipath.modules.shared.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class ResetPasswordViewModel(
    application: Application
): AndroidViewModel(application){
    private val authService = RetrofitInstance.authService

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _resetError = MutableStateFlow<String?>(null)
    val resetError: StateFlow<String?> = _resetError.asStateFlow()

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess.asStateFlow()

    private val context = getApplication<Application>()

    private fun validateAndGetString(validationFunc: () -> Int?): String? {
        return validationFunc()?.let { context.getString(it) }
    }

    fun onEmailChanged(value: String) {
        _email.value = value
        if (_emailError.value != null) {
            validateEmail()
        }
    }

    fun validateEmail() {
        _emailError.value = validateAndGetString { ValidationUtils.validateEmail(_email.value) }
    }

    fun resetPassword() {
        viewModelScope.launch {
            _resetError.value = ""
            _resetSuccess.value = false
            try {
                val response = authService.resetPassword(_email.value)
                if (response.isSuccessful) {
                    _resetSuccess.value = true
                } else {
                    _resetError.value = when (response.code()) {
                        400 -> context.getString(R.string.please_fill_email_field)
                        503 -> context.getString(R.string.the_email_service_threw_an_error)
                        else -> context.getString(R.string.an_unknown_error_occurred_please_try_again_later)
                    }
                }
            } catch (_: IOException) {
                _resetError.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _resetError.value = context.getString(R.string.unknown_error)
            }
        }
    }

    fun clearError() {
        _resetError.value = ""
        _resetSuccess.value = false
    }
}