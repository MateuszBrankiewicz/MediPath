package com.medipath.modules.patient.search

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.InstitutionDoctor
import com.medipath.core.models.InstitutionDetail
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.models.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import com.medipath.R
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException

class InstitutionDetailsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val searchService = RetrofitInstance.searchService

    private val _doctors = MutableStateFlow<List<InstitutionDoctor>>(emptyList())
    val doctors: StateFlow<List<InstitutionDoctor>> = _doctors

    private val _institution = MutableStateFlow<InstitutionDetail?>(null)
    val institution: StateFlow<InstitutionDetail?> = _institution

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin

    private val context = getApplication<Application>()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadInstitutionData(institutionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _shouldRedirectToLogin.value = false

            try {
                val institutionJob = async { fetchInstitution(institutionId) }
                val doctorsJob = async { fetchDoctors(institutionId) }
                val commentsJob = async { fetchComments(institutionId) }

                awaitAll(institutionJob, doctorsJob, commentsJob)

            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchInstitution(institutionId: String) {
        try {
            val response = searchService.getInstitution(institutionId, null)

            if (response.isSuccessful) {
                _institution.value = response.body()?.institution
            } else if (response.code() == 401) {
                _shouldRedirectToLogin.value = true
            } else {
                _error.value = context.getString(R.string.error_load_institutions)
            }
        } catch (_: IOException) {
            _error.value = context.getString(R.string.error_connection)
        } catch (_: Exception) {
            _error.value = context.getString(R.string.unknown_error)
        }
    }

    private suspend fun fetchDoctors(institutionId: String) {
        try {
            val response = searchService.getInstitutionDoctors(institutionId)

            if (response.isSuccessful) {
                _doctors.value = response.body()?.doctors ?: emptyList()
            } else if (response.code() == 401) {
                _shouldRedirectToLogin.value = true
            } else {
                _error.value = context.getString(R.string.error_load_doctors)
            }
        } catch (_: IOException) {
            _error.value = context.getString(R.string.error_connection)
        } catch (_: Exception) {
            _error.value = context.getString(R.string.unknown_error)
        }
    }

    private suspend fun fetchComments(institutionId: String) {
        try {
            val response = RetrofitInstance.commentsService.getInstitutionComments(institutionId)

            if (response.isSuccessful) {
                _comments.value = response.body()?.comments ?: emptyList()
            } else if (response.code() == 401) {
                _shouldRedirectToLogin.value = true
            } else {
                _error.value = context.getString(R.string.error_load_comments)
            }
        } catch (_: IOException) {
            _error.value = context.getString(R.string.error_connection)
        } catch (_: Exception) {
            _error.value = context.getString(R.string.unknown_error)
        }
    }
}