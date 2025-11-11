package com.medipath.modules.patient.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.SearchService
import com.medipath.core.models.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchService: SearchService = RetrofitInstance.searchService
) : ViewModel() {
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun search(query: String, type: String, city: String, specialisation: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = searchService.search(
                    query = query,
                    type = type,
                    city = city,
                    specialisations = specialisation
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