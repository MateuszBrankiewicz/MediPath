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

    private val _deleteSuccess = mutableStateOf(false)
    val deleteSuccess: State<Boolean> = _deleteSuccess

    private val _deleteError = mutableStateOf("")
    val deleteError: State<String> = _deleteError

    private val _prescriptionCode = mutableStateOf("")
    val prescriptionCode: State<String> = _prescriptionCode

    private val _referralCode = mutableStateOf("")
    val referralCode: State<String> = _referralCode

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
                fetchActiveCodes(token)
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

    private suspend fun fetchActiveCodes(token: String) {
        try {
            if (_userId.value.isEmpty()) {
                Log.e("HomeViewModel", "UserId is empty!")
                return
            }

            val userId = _userId.value

            val codesResponse = apiService.getActiveCodes(userId)

            if (codesResponse.isSuccessful) {
                val codes = codesResponse.body()?.codes ?: emptyList()
                Log.d("HomeViewModel", "Received ${codes.size} codes")

                val prescriptions = codes.filter { it.codes.codeType == "PRESCRIPTION" }
                val referrals = codes.filter { it.codes.codeType == "REFERRAL" }

                _prescriptionCode.value = prescriptions.lastOrNull()?.codes?.code ?: ""
                _referralCode.value = referrals.lastOrNull()?.codes?.code ?: ""

                Log.d("HomeViewModel", "Final codes: Prescription=${_prescriptionCode.value}, Referral=${_referralCode.value}")
            } else {
                Log.e("HomeViewModel", "API error: ${codesResponse.code()} - ${codesResponse.message()}")
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching codes: $e")
        }
    }

    fun getCurrentUserId(): String {
        return _userId.value
    }

    fun cancelVisit(visitId: String, sessionManager: DataStoreSessionManager) {
        viewModelScope.launch {
            try {
                if (visitId.isEmpty()) {
                    _deleteError.value = "Nieprawidłowy ID wizyty"
                    return@launch
                }

                val token = sessionManager.getSessionId()
                if (token.isNullOrEmpty()) {
                    _deleteError.value = "Brak sesji użytkownika"
                    return@launch
                }

                Log.d("HomeViewModel", "Cancelling visit with ID: $visitId")
                val response = apiService.cancelVisit(visitId, "SESSION=$token")

                if (response.isSuccessful) {
                    _deleteSuccess.value = true
                    Log.d("HomeViewModel", "Visit cancelled successfully")
                    fetchUpcomingVisits(token)
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Wizyta została już zakończona lub anulowana"
                        401 -> "Sesja wygasła, zaloguj się ponownie"
                        403 -> "Brak uprawnień do anulowania tej wizyty"
                        else -> "Błąd podczas anulowania wizyty (${response.code()})"
                    }
                    _deleteError.value = errorMessage
                    Log.e("HomeViewModel", "Cancel visit error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _deleteError.value = "Błąd sieciowy: ${e.message}"
                Log.e("HomeViewModel", "Error cancelling visit", e)
            }
        }
    }

    fun clearDeleteMessages() {
        _deleteSuccess.value = false
        _deleteError.value = ""
    }
}
