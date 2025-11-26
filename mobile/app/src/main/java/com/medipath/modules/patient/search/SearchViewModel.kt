package com.medipath.modules.patient.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.models.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class SearchViewModel(
    application: Application
) : AndroidViewModel(application){
    private val searchService = RetrofitInstance.searchService

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val context = getApplication<Application>()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
                } else{
                    _error.value = context.getString(R.string.error_fetch_search)
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
            } finally {
                _isLoading.value = false
            }
        }
    }
}