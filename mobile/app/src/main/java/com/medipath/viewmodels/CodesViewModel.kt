package com.medipath.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.data.api.ApiService
import com.medipath.data.api.RetrofitInstance
import kotlinx.coroutines.launch
import android.util.Log
import com.medipath.data.models.CodeItem

class CodesViewModel(
    private val apiService: ApiService = RetrofitInstance.api
) : ViewModel() {

    private val _codes = mutableStateOf<List<CodeItem>>(emptyList())
    val codes: State<List<CodeItem>> = _codes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf("")
    val error: State<String> = _error

    fun fetchCodes(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = ""

                val response = apiService.getActiveCodes(userId)
                if (response.isSuccessful) {
                    _codes.value = response.body()?.codes ?: emptyList()
                    Log.d("CodesViewModel", "Fetched ${_codes.value.size} codes")
                } else {
                    _error.value = when (response.code()) {
                        400 -> "Nieprawidłowy ID użytkownika"
                        else -> "Błąd podczas pobierania kodów (${response.code()})"
                    }
                    Log.e("CodesViewModel", "Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _error.value = "Błąd sieciowy: ${e.message}"
                Log.e("CodesViewModel", "Error fetching codes", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = ""
    }
}