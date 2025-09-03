package com.medipath.viewmodels

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import com.medipath.data.api.RetrofitInstance
import androidx.compose.runtime.mutableStateOf
import com.medipath.data.api.ApiService

class ResetPasswordViewModel(
    private val apiService: ApiService = RetrofitInstance.api
): ViewModel() {

    private val _resetError = mutableStateOf("")
    val resetError: State<String> = _resetError

    private val _resetSuccess = mutableStateOf(false)
    val resetSuccess: State<Boolean> = _resetSuccess

    fun resetPassword(email: String) {

    }

    fun clearError() {
        _resetError.value = ""
        _resetSuccess.value = false
    }
}