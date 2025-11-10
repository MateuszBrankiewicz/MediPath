package com.medipath.modules.shared.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.AuthService
import com.medipath.core.utils.ValidationUtils
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ResetPasswordViewModel(
    private val authService: AuthService = RetrofitInstance.authService
): ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    private val _resetError = mutableStateOf<String?>(null)
    val resetError: State<String?> = _resetError

    private val _resetSuccess = mutableStateOf(false)
    val resetSuccess: State<Boolean> = _resetSuccess

    val isFormValid: State<Boolean> = derivedStateOf {
        email.value.isNotBlank() && emailError.value == null
    }

    fun onEmailChanged(value: String) {
        _email.value = value
        if (_emailError.value != null) {
            validateEmail()
        }
    }

    fun validateEmail() {
        _emailError.value = ValidationUtils.validateEmail(email.value).ifEmpty { null }
    }

    fun resetPassword() {
        viewModelScope.launch {
            _resetError.value = ""
            _resetSuccess.value = false
            try {
                val response = authService.resetPassword(email.value)
                if (response.isSuccessful) {
                    _resetSuccess.value = true
                } else {
                    _resetError.value = response.body()?.message ?: "An unknown error occurred"
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> {
                        _resetError.value = "Please fill email field."
                    }
                    500 -> {
                        _resetError.value = "The email service threw an error."
                    }
                    else -> {
                        _resetError.value = "An unknown error occurred. Please try again later."
                    }
                }
            } catch (e: Exception) {
                _resetError.value = "Reset failed"
            }
        }
    }

    fun clearError() {
        _resetError.value = ""
        _resetSuccess.value = false
    }
}