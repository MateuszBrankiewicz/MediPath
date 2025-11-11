package com.medipath.modules.patient.visits

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.AddCommentRequest
import com.medipath.core.models.UpdateCommentRequest
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewVisitViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess.asStateFlow()
    
    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

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

                Log.d("ReviewVisitViewModel", "Submitting review - visitId: $visitId, commentId: $commentId")

                val response = if (commentId != null && commentId.isNotEmpty()) {
                    Log.d("ReviewVisitViewModel", "Updating comment: $commentId")
                    val updateRequest = UpdateCommentRequest(
                        institutionRating = institutionRating,
                        doctorRating = doctorRating,
                        comment = comments
                    )
                    RetrofitInstance.commentsService.updateComment(commentId, updateRequest)
                } else {
                    Log.d("ReviewVisitViewModel", "Adding new comment")
                    val addRequest = AddCommentRequest(
                        visitID = visitId,
                        institutionRating = institutionRating,
                        doctorRating = doctorRating,
                        comment = comments
                    )
                    RetrofitInstance.commentsService.addComment(addRequest)
                }

                Log.d("ReviewVisitViewModel", "Response code: ${response.code()}")

                when (response.code()) {
                    200, 201 -> {
                        _submitSuccess.value = true
                    }
                    400 -> {
                        _error.value = "Invalid rating. Rating must be between 1-5 in 0.5 increments"
                    }
                    401 -> {
                        _shouldRedirectToLogin.value = true
                    }
                    403 -> {
                        _error.value = if (commentId != null) {
                            "Comment does not exist or does not belong to you"
                        } else {
                            "You do not have permission to add a review for this visit"
                        }
                    }
                    404 -> {
                        _error.value = if (commentId != null) {
                            "Comment not found"
                        } else {
                            "Visit not found or already has a review"
                        }
                    }
                    500 -> {
                        _error.value = "Server error. Please try again later"
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string()
                        _error.value = "Error ${response.code()}: ${errorBody ?: "Unknown error"}"
                    }
                }
                
            } catch (e: Exception) {
                _error.value = "Error sending review: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
