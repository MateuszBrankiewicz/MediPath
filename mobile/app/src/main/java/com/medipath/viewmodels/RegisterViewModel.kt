package com.medipath.viewmodels

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.data.api.RetrofitInstance
import com.medipath.data.models.City
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.medipath.data.models.RegisterRequest
import retrofit2.HttpException

class RegisterViewModel: ViewModel() {
    private val _cities = mutableStateOf<List<City>>(emptyList())
    val cities: State<List<City>> = _cities

    private val _provinces = mutableStateOf<List<String>>(emptyList())
    val provinces: State<List<String>> = _provinces

    private val _registrationError = mutableStateOf("")
    val registrationError: State<String> = _registrationError

    private val _registrationSuccess = mutableStateOf(false)
    val registrationSuccess: State<Boolean> = _registrationSuccess

    init {
        fetchCities()
        fetchProvinces()
    }

    private fun fetchCities() {
        viewModelScope.launch {
            try {
                _cities.value = RetrofitInstance.api.getCities()
                Log.d("RegisterViewModel", "Pobrano miasta: ${_cities.value}")
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Błąd podczas pobierania miast: ${e.message}", e)
                _cities.value = emptyList()
            }
        }
    }

    private fun fetchProvinces() {
        viewModelScope.launch {
            try {
                _provinces.value = RetrofitInstance.api.getProvinces()
                Log.d("RegisterViewModel", "Pobrano wojewódźtwa: ${_provinces.value}")
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Błąd podczas pobierania wojewódźtw: ${e.message}", e)
                _provinces.value = emptyList()
            }
        }
    }

    fun registerUser(request: RegisterRequest) {
        viewModelScope.launch {
            _registrationError.value = ""
            _registrationSuccess.value = false

            try {
                val response = RetrofitInstance.api.registerUser(request)
                _registrationSuccess.value = true
                Log.d("RegisterViewModel", "Registration successful: $response")
            } catch (e: HttpException) {
                when (e.code()) {
                    400 -> {
                        _registrationError.value = "Please fill in all required fields."
                        Log.e("RegisterViewModel", "Missing fields: ${e.response()?.errorBody()?.string()}")
                    }
                    409 -> {
                        _registrationError.value = "An account with this email or GovID already exists."
                        Log.e("RegisterViewModel", "User already exists")
                    }
                    else -> {
                        _registrationError.value = "An unknown error occurred. Please try again later."
                        Log.e("RegisterViewModel", "Unknown error: ${e.message()}")
                    }
                }
            } catch (e: Exception) {
                _registrationError.value = "Registration failed"
                Log.e("RegisterViewModel", "Network error: ${e.message}", e)
            }
        }
    }

    fun clearError() {
        _registrationError.value = ""
        _registrationSuccess.value = false
    }
}