package com.medipath.viewmodels

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.data.api.RetrofitInstance
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.medipath.data.api.ApiService
import com.medipath.data.models.LoginRequest
import retrofit2.HttpException

class LoginViewModel(
    private val apiService: ApiService = RetrofitInstance.api
): ViewModel() {

    private val _loginError = mutableStateOf("")
    val loginError: State<String> = _loginError

    private val _loginSuccess = mutableStateOf(false)
    val loginSuccess: State<Boolean> = _loginSuccess

    private val _sessionId = mutableStateOf("")
    val sessionId: State<String> = _sessionId

    fun loginUser(request: LoginRequest) {
        viewModelScope.launch {
            _loginError.value = ""
            _loginSuccess.value = false
            _sessionId.value = ""

            try {
                val response = apiService.loginUser(request)
                if (response.isSuccessful) {
                    val sessionId = response.headers()["Set-Cookie"]?.let { cookie ->
                        Log.d("LoginViewModel", "Full cookie: '$cookie'")
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
                    Log.d("LoginViewModel", "Login successful, session ID: $sessionId")
                } else {
                    _loginError.value = "Invalid credentials"
                }
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> {
                        _loginError.value = "Please fill in all required fields."
                        Log.e("LoginViewModel", "Missing fields: ${e.response()?.errorBody()?.string()}")
                    }
                    401 -> {
                        _loginError.value = "Invalid email or password."
                        Log.e("LoginViewModel", "User with the email does not exist or the password does not match")
                    }
                    else -> {
                        _loginError.value = "An unknown error occurred. Please try again later."
                        Log.e("LoginViewModel", "Unknown error: ${e.message()}")
                    }
                }
            } catch (e: Exception) {
                _loginError.value = "Registration failed"
                Log.e("LoginViewModel", "Network error: ${e.message}", e)
            }
        }
    }

    fun clearError() {
        _loginError.value = ""
        _loginSuccess.value = false
    }
}