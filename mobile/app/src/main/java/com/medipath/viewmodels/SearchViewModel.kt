package com.medipath.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.data.api.RetrofitInstance
import com.medipath.data.api.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun search(query: String, type: String, city: String, specialisation: String) {
        Log.d("SearchViewModel", "Searching with query: $query, type: $type, city: $city, specialisation: $specialisation")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.search(
                    query = query,
                    type = type,
                    city = city,
                    specialisation = specialisation
                )
                if (response.isSuccessful) {
                    _searchResults.value = response.body()?.result ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}