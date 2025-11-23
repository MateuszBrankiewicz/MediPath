package com.medipath.modules.patient.visits

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

class CommentDetailsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val commentsService = RetrofitInstance.commentsService

    private val _comment = MutableStateFlow<Comment?>(null)
    val comment: StateFlow<Comment?> = _comment.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    private val _deleteLoading = MutableStateFlow(false)
    val deleteLoading: StateFlow<Boolean> = _deleteLoading.asStateFlow()
    
    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()
    
    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()
    
    private val context = getApplication<Application>()

    fun fetchComment(commentId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _shouldRedirectToLogin.value = false

                val response = commentsService.getComment(commentId)
                if (response.isSuccessful) {
                    _comment.value = response.body()?.comment
                } else {
                    _error.value = context.getString(R.string.error_load_comment)
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

    fun deleteComment(commentId: String) {
        viewModelScope.launch {
            try {
                _deleteLoading.value = true
                _deleteError.value = null
                _deleteSuccess.value = false
                _shouldRedirectToLogin.value = false

                val response = commentsService.deleteComment(commentId)

                if (response.isSuccessful) {
                    _deleteSuccess.value = true
                } else {
                    _deleteError.value = context.getString(R.string.error_delete_comment)
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
            } finally {
                _deleteLoading.value = false
            }
        }
    }
}
