package com.medipath.modules.shared.auth

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.medipath.core.models.City
import com.medipath.core.models.RegisterRequest
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.AuthService
import com.medipath.core.services.LocationService
import retrofit2.HttpException

class RegisterViewModel(
    private val authService: AuthService = RetrofitInstance.authService,
    private val locationService: LocationService = RetrofitInstance.locationService
): ViewModel() {
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
                _cities.value = locationService.getCities()
                Log.d("RegisterViewModel", "Fetched cities: ${_cities.value}")
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error fetching cities", e)
                _cities.value = emptyList()
            }
        }
    }

    private fun fetchProvinces() {
        viewModelScope.launch {
            try {
                _provinces.value = locationService.getProvinces()
                Log.d("RegisterViewModel", "Fetched provinces: ${_provinces.value}")
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error fetching provinces", e)
                _provinces.value = emptyList()
            }
        }
    }

    fun registerUser(request: RegisterRequest) {
        viewModelScope.launch {
            _registrationError.value = ""
            _registrationSuccess.value = false

            try {
                val response = authService.registerUser(request)
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