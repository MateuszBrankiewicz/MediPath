package com.medipath.viewmodels

import android.util.Log
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.medipath.data.api.RetrofitInstance
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.medipath.data.api.ApiService
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ResetPasswordViewModel(
    private val apiService: ApiService = RetrofitInstance.api
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
                val response = apiService.resetPassword(email)
                _resetSuccess.value = true
                Log.d("resetViewModel", "Successful: $response")
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> {
                        _resetError.value = "Please fill email field."
                        Log.e("resetViewModel", "Missing email: ${e.response()?.errorBody()?.string()}")
                    }
                    500 -> {
                        _resetError.value = "The email service threw an error."
                        Log.e("resetViewModel", "Unexpected error from email service: ${e.response()?.errorBody()?.string()}")
                    }
                    else -> {
                        _resetError.value = "An unknown error occurred. Please try again later."
                        Log.e("resetViewModel", "Unknown error: ${e.message()}")
                    }
                }
            } catch (e: Exception) {
                _resetError.value = "Reset failed"
                Log.e("resetViewModel", "Network error: ${e.message}", e)
            }
        }
    }

    fun clearError() {
        _resetError.value = ""
        _resetSuccess.value = false
    }
}