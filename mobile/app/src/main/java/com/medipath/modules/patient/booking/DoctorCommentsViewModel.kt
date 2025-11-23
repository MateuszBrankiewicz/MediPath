package com.medipath.modules.patient.booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.Comment
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class DoctorCommentsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val searchService = RetrofitInstance.searchService

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val context = getApplication<Application>()

    fun loadComments(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = searchService.getDoctorComments(doctorId)
                if (response.isSuccessful) {
                    _comments.value = response.body()?.comments ?: emptyList()
                } else{
                    _error.value = context.getString(R.string.error_load_comments)
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