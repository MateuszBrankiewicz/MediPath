package com.medipath.modules.patient.codes

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.services.UserService
import kotlinx.coroutines.launch
import com.medipath.core.models.CodeItem
import com.medipath.core.network.RetrofitInstance

class CodesViewModel(
    private val userService: UserService = RetrofitInstance.userService
) : ViewModel() {

    private val _codes = mutableStateOf<List<CodeItem>>(emptyList())
    val codes: State<List<CodeItem>> = _codes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf("")
    val error: State<String> = _error

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
                        400 -> "Nieprawidłowy typ kodu"
                        401 -> "Sesja wygasła, zaloguj się ponownie"
                        else -> "Błąd podczas pobierania kodów (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Błąd sieciowy: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = ""
    }
}