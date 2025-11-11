package com.medipath.modules.patient.booking

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.Comment
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DoctorCommentsViewModel : ViewModel() {
    private val searchService = RetrofitInstance.searchService

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadComments(doctorId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = searchService.getDoctorComments(doctorId)
                if (response.isSuccessful) {
                    _comments.value = response.body()?.comments ?: emptyList()
                }
            } catch (e: Exception) {
                Log.d("TODO", "Dodaj kody bledow")
            } finally {
                _isLoading.value = false
            }
        }
    }
}