package com.medipath.modules.shared.auth

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.medipath.core.models.LoginRequest
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.AuthService
import retrofit2.HttpException


class LoginViewModel(
    private val authService: AuthService = RetrofitInstance.authService
): ViewModel() {

    private val _loginError = mutableStateOf("")
    val loginError: State<String> = _loginError

    private val _loginSuccess = mutableStateOf(false)
    val loginSuccess: State<Boolean> = _loginSuccess

    private val _sessionId = mutableStateOf("")
    val sessionId: State<String> = _sessionId

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun loginUser(request: LoginRequest) {
        viewModelScope.launch {
            _loginError.value = ""
            _loginSuccess.value = false
            _sessionId.value = ""
            _isLoading.value = true
            try {
                val response = authService.loginUser(request)
                if (response.isSuccessful) {
                    val sessionId = response.headers()["Set-Cookie"]?.let { cookie ->
                        cookie.split(";")
                            .map { it.trim() }
                            .firstOrNull { it.startsWith("SESSION=") }
                            ?.substringAfter("SESSION=")
                            ?.also {
                                Log.d("LoginViewModel", "Extracted SESSION: '$it'")
                            }
                    }
                    _sessionId.value = sessionId ?: ""
                    _loginSuccess.value = true
                } else {
                    _loginError.value = "Invalid credentials"
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> {
                        _loginError.value = "Please fill in all required fields."
                    }
                    401 -> {
                        _loginError.value = "Invalid email or password."
                    }
                    else -> {
                        _loginError.value = "An unknown error occurred. Please try again later."
                    }
                }
            } catch (e: Exception) {
                _loginError.value = "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _loginError.value = ""
        _loginSuccess.value = false
    }
}