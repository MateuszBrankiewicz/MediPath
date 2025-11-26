package com.medipath.modules.patient.visits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.AddCommentRequest
import com.medipath.core.models.UpdateCommentRequest
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class ReviewVisitViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()
    
    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()
    
    private val context = getApplication<Application>()

    fun submitReview(
        visitId: String,
        doctorRating: Double,
        institutionRating: Double,
        comments: String,
        commentId: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _shouldRedirectToLogin.value = false
                _submitSuccess.value = false

                val response = if (commentId != null && commentId.isNotEmpty()) {
                    val updateRequest = UpdateCommentRequest(
                        institutionRating = institutionRating,
                        doctorRating = doctorRating,
                        comment = comments
                    )
                    RetrofitInstance.commentsService.updateComment(commentId, updateRequest)
                } else {
                    val addRequest = AddCommentRequest(
                        visitID = visitId,
                        institutionRating = institutionRating,
                        doctorRating = doctorRating,
                        comment = comments
                    )
                    RetrofitInstance.commentsService.addComment(addRequest)
                }
                
                when (response.code()) {
                    200, 201 -> {
                        _submitSuccess.value = true
                    }
                    400 -> {
                        _error.value = context.getString(R.string.error_invalid_rating)
                    }
                    401 -> {
                        _shouldRedirectToLogin.value = true
                    }
                    403 -> {
                        _error.value = if (commentId != null) {
                            context.getString(R.string.error_comment_does_not_exist)
                        } else {
                            context.getString(R.string.error_no_permission)
                        }
                    }
                    404 -> {
                        _error.value = if (commentId != null) {
                            context.getString(R.string.error_comment_not_found)
                        } else {
                            context.getString(R.string.error_visit_not_found)
                        }
                    }
                    500 -> {
                        _error.value = context.getString(R.string.error_server)
                    }
                    else -> {
                        _error.value = context.getString(R.string.error_submit_review)
                    }
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
