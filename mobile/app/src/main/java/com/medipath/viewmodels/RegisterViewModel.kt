package com.medipath.viewmodels

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.data.api.RetrofitInstance
import com.medipath.data.models.City
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.mutableStateOf

class RegisterViewModel: ViewModel() {
    private val _cities = mutableStateOf<List<City>>(emptyList())
    val cities: State<List<City>> = _cities

    init {
        fetchCities()
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
}
