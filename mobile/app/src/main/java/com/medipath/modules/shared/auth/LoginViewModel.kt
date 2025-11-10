package com.medipath.modules.shared.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import com.medipath.core.models.LoginRequest
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.AuthService
import com.medipath.core.utils.ValidationUtils
import retrofit2.HttpException


class LoginViewModel(
    private val authService: AuthService = RetrofitInstance.authService
): ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError

    private val _passwordError = mutableStateOf<String?>(null)
    val passwordError: State<String?> = _passwordError

    private val _loginError = mutableStateOf<String?>(null)
    val loginError: State<String?> = _loginError

    private val _loginSuccess = mutableStateOf(false)
    val loginSuccess: State<Boolean> = _loginSuccess

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    val isFormValid: State<Boolean> = derivedStateOf {
        email.value.isNotBlank() &&
                password.value.isNotBlank() &&
                emailError.value == null &&
                passwordError.value == null
    }
    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
        if (_emailError.value != null) {
            validateEmail()
        }
    }

    fun onPasswordChanged(newPass: String) {
        _password.value = newPass
        if (_passwordError.value != null) {
            validatePassword()
        }
    }

    fun validateEmail() {
        _emailError.value = ValidationUtils.validateEmail(email.value).ifEmpty { null }
    }

    fun validatePassword() {
        _passwordError.value = ValidationUtils.validatePassword(password.value).ifEmpty { null }
    }
    fun loginUser() {
        val request = LoginRequest(
            email = email.value,
            password = password.value
        )

        viewModelScope.launch {
            _loginError.value = null
            _loginSuccess.value = false
            _isLoading.value = true
            try {
                val response = authService.loginUser(request)
                if (response.isSuccessful) {
                    _loginSuccess.value = true
                } else {
                    _loginError.value = "Invalid credentials"
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> _loginError.value = "Please fill in all required fields."
                    401 -> _loginError.value = "Invalid email or password."
                    else -> _loginError.value = "An unknown error occurred. Please try again later."
                }
            } catch (e: Exception) {
                _loginError.value = e.message ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _loginError.value = null
        _loginSuccess.value = false
    }
}