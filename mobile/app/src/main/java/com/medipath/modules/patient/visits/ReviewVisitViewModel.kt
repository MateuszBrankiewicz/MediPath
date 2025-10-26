package com.medipath.modules.patient.visits

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.AddCommentRequest
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.launch

class ReviewVisitViewModel : ViewModel() {
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val submitSuccess = mutableStateOf(false)

    fun submitReview(
        visitId: String,
        doctorRating: Double,
        institutionRating: Double,
        comments: String,
        sessionManager: DataStoreSessionManager
    ) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                error.value = null
                
                val token = sessionManager.getSessionId()
                val cookie = "SESSION=$token"
                
                val commentRequest = AddCommentRequest(
                    visitID = visitId,
                    institutionRating = institutionRating,
                    doctorRating = doctorRating,
                    comment = comments
                )
                
                val response = RetrofitInstance.commentsService.addComment(commentRequest, cookie)
                
                when (response.code()) {
                    201 -> {
                        submitSuccess.value = true
                    }
                    400 -> {
                        error.value = "Invalid rating. Rating must be between 1-5 in 0.5 increments"
                    }
                    401 -> {
                        error.value = "Session expired. Please log in again"
                    }
                    403 -> {
                        error.value = "You do not have permission to add a review for this visit"
                    }
                    500 -> {
                        error.value = "Server error. Please try again later"
                    }
                    else -> {
                        error.value = "Unexpected error: ${response.code()}"
                    }
                }
                
            } catch (e: Exception) {
                error.value = "Error sending review: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}
