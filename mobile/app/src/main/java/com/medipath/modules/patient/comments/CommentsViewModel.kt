package com.medipath.modules.patient.comments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.UserComment
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommentsViewModel : ViewModel() {
    private val commentsService = RetrofitInstance.commentsService

    private val _comments = MutableStateFlow<List<UserComment>>(emptyList())
    val comments: StateFlow<List<UserComment>> = _comments.asStateFlow()

    private val _filteredComments = MutableStateFlow<List<UserComment>>(emptyList())
    val filteredComments: StateFlow<List<UserComment>> = _filteredComments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow("Date")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow("Descending")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _totalComments = MutableStateFlow(0)
    val totalComments: StateFlow<Int> = _totalComments.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    fun fetchComments() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _shouldRedirectToLogin.value = false

                val response = commentsService.getUserComments()

                if (response.isSuccessful) {
                    _comments.value = response.body()?.comments ?: emptyList()
                    _totalComments.value = _comments.value.size
                    applyFilters()
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = "Failed to load comments: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("CommentsViewModel", "Error fetching comments", e)
                _error.value = "Failed to load comments: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                _error.value = null
                _deleteSuccess.value = false

                val response = commentsService.deleteComment(commentId)

                if (response.isSuccessful) {
                    _comments.value = _comments.value.filter { it.id != commentId }
                    _totalComments.value = _comments.value.size
                    applyFilters()
                    _deleteSuccess.value = true
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = "Failed to delete comment: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to delete comment: ${e.message}"
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updateSortBy(sort: String) {
        _sortBy.value = sort
        applyFilters()
    }

    fun updateSortOrder(order: String) {
        _sortOrder.value = order
        applyFilters()
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _sortBy.value = "Date"
        _sortOrder.value = "Descending"
        applyFilters()
    }

    private fun applyFilters() {
        var filtered = _comments.value

        if (_searchQuery.value.isNotEmpty()) {
            val query = _searchQuery.value.lowercase()
            filtered = filtered.filter {
                it.doctor.lowercase().contains(query) ||
                it.institution.lowercase().contains(query) ||
                it.content.lowercase().contains(query)
            }
        }

        filtered = when (_sortBy.value) {
            "Date" -> filtered.sortedBy { it.createdAt }
            "Doctor Rating" -> filtered.sortedBy { it.doctorRating }
            "Institution Rating" -> filtered.sortedBy { it.institutionRating }
            "Doctor Name" -> filtered.sortedBy { it.doctor }
            "Institution Name" -> filtered.sortedBy { it.institution }
            else -> filtered.sortedBy { it.createdAt }
        }

        if (_sortOrder.value == "Descending") {
            filtered = filtered.reversed()
        }

        _filteredComments.value = filtered
    }
}
