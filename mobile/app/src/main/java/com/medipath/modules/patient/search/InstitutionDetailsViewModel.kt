package com.medipath.modules.patient.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.InstitutionDoctor
import com.medipath.core.models.InstitutionDetail
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.models.Comment
import com.medipath.core.services.SearchService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

class InstitutionDetailsViewModel(
    private val searchService: SearchService = RetrofitInstance.searchService
) : ViewModel() {

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

    private fun handleAuthError(e: Exception) {
        if (e is HttpException && e.code() == 401) {
            _shouldRedirectToLogin.value = true
        }
    }

    fun loadInstitutionData(institutionId: String) {
        Log.d("InstitutionDetailsVM", "=== loadInstitutionData START for institutionId=$institutionId ===")
        viewModelScope.launch {
            _isLoading.value = true
            _shouldRedirectToLogin.value = false

            try {
                val institutionJob = async { fetchInstitution(institutionId) }
                val doctorsJob = async { fetchDoctors(institutionId) }
                val commentsJob = async { fetchComments(institutionId) }

                awaitAll(institutionJob, doctorsJob, commentsJob)

            } catch (e: Exception) {
                Log.e("InstitutionDetailsVM", "Main exception in loadInstitutionData: ${e.message}", e)
                handleAuthError(e)
            } finally {
                _isLoading.value = false
                Log.d("InstitutionDetailsVM", "=== loadData END - doctors=${_doctors.value.size}, institution=${_institution.value?.name}, comments=${_comments.value.size} ===")
            }
        }
    }

    private suspend fun fetchInstitution(institutionId: String) {
        try {
            val response = searchService.getInstitution(institutionId, null)

            if (response.isSuccessful) {
                _institution.value = response.body()?.institution
                Log.d("InstitutionDetailsVM", "fetchInstitution OK: ${response.body()?.institution?.name}")
            } else if (response.code() == 401) {
                _shouldRedirectToLogin.value = true
            } else {
                Log.e("InstitutionDetailsVM", "fetchInstitution failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("InstitutionDetailsVM", "fetchInstitution exception", e)
            handleAuthError(e)
        }
    }

    private suspend fun fetchDoctors(institutionId: String) {
        try {
            val response = searchService.getInstitutionDoctors(institutionId)

            if (response.isSuccessful) {
                _doctors.value = response.body()?.doctors ?: emptyList()
                Log.d("InstitutionDetailsVM", "fetchDoctors OK: ${_doctors.value.size} doctors")
            } else if (response.code() == 401) {
                _shouldRedirectToLogin.value = true
            } else {
                Log.e("InstitutionDetailsVM", "fetchDoctors failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("InstitutionDetailsVM", "fetchDoctors exception", e)
            handleAuthError(e)
        }
    }

    private suspend fun fetchComments(institutionId: String) {
        try {
            val response = RetrofitInstance.commentsService.getInstitutionComments(institutionId)

            if (response.isSuccessful) {
                _comments.value = response.body()?.comments ?: emptyList()
                Log.d("InstitutionDetailsVM", "fetchComments OK: ${_comments.value.size} comments")
            } else if (response.code() == 401) {
                _shouldRedirectToLogin.value = true
            } else {
                Log.e("InstitutionDetailsVM", "fetchComments failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("InstitutionDetailsVM", "fetchComments exception", e)
            handleAuthError(e)
        }
    }
}