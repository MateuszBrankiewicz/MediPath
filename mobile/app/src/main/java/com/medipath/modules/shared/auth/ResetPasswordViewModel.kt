package com.medipath.modules.shared.auth

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.AuthService
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ResetPasswordViewModel(
    private val authService: AuthService = RetrofitInstance.authService
): ViewModel() {

    private val _resetError = mutableStateOf("")
    val resetError: State<String> = _resetError

    private val _resetSuccess = mutableStateOf(false)
    val resetSuccess: State<Boolean> = _resetSuccess

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetError.value = ""
            _resetSuccess.value = false
            try {
                val response = authService.resetPassword(email)
                _resetSuccess.value = true
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