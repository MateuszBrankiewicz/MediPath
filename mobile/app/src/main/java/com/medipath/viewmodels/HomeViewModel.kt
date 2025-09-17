package com.medipath.viewmodels

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.data.api.RetrofitInstance
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.medipath.data.api.ApiService
import com.medipath.data.api.DataStoreSessionManager
import com.medipath.data.models.Visit

class HomeViewModel(
    private val apiService: ApiService = RetrofitInstance.api
) : ViewModel() {

    private val _firstName = mutableStateOf("")
    val firstName: State<String> = _firstName

    private val _upcomingVisits = mutableStateOf<List<Visit>>(emptyList())
    val upcomingVisits: State<List<Visit>> = _upcomingVisits

    private val _userId = mutableStateOf("")

    fun fetchUserProfile(sessionManager: DataStoreSessionManager) {
        viewModelScope.launch {
            try {
                val token = sessionManager.getSessionId()
                if(token.isNullOrEmpty()) {
                    Log.e("HomeViewModel", "No session token found")
                    return@launch
                }
                Log.d("HomeViewModel", "Using token: $token")
                val userResponse = apiService.getUserProfile("SESSION=$token")
                _firstName.value = userResponse.user.name
                _userId.value = userResponse.user.id
                Log.d("HomeViewModel", "Fetched user profile: ${userResponse.user.name}, ID: ${userResponse.user.id}")
                fetchUpcomingVisits(token)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error: $e")
            }
        }
    }

    private suspend fun fetchUpcomingVisits(token: String) {
        try {
            Log.d("HomeViewModel", "Fetching visits for userId: ${_userId.value}")
            Log.d("HomeViewModel", "Full URL would be: visits/upcoming/${_userId.value}")

            if (_userId.value.isEmpty()) {
                Log.e("HomeViewModel", "UserId is empty!")
                return
            }

            val visitsResponse = apiService.getUpcomingVisits(_userId.value, "SESSION=$token")
            _upcomingVisits.value = visitsResponse.visits
            Log.d("HomeViewModel", "Fetched ${visitsResponse.visits.size} upcoming visits")
        } catch (e: retrofit2.HttpException) {
            Log.e("HomeViewModel", "HTTP Error ${e.code()}: ${e.message()}")
            Log.e("HomeViewModel", "Error body: ${e.response()?.errorBody()?.string()}")
            try {
                Log.e("HomeViewModel", "Error body: ${e.response()?.errorBody()?.string()} ${upcomingVisits.value}")
            } catch (ex: Exception) {
                Log.e("HomeViewModel", "Could not read error body")
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching visits: $e")
        }
    }
}
