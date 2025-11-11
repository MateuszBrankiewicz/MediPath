package com.medipath.modules.patient.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.medipath.core.models.Visit
import com.medipath.core.services.UserService
import com.medipath.core.services.VisitsService
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    private val userService: UserService = RetrofitInstance.userService,
    private val visitsService: VisitsService = RetrofitInstance.visitsService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()

    private val _upcomingVisits = MutableStateFlow<List<Visit>>(emptyList())
    val upcomingVisits: StateFlow<List<Visit>> = _upcomingVisits.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _prescriptionCode = MutableStateFlow("")
    val prescriptionCode: StateFlow<String> = _prescriptionCode.asStateFlow()

    private val _referralCode = MutableStateFlow("")
    val referralCode: StateFlow<String> = _referralCode.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    private fun handleAuthError(error: Exception) {
        if (error is retrofit2.HttpException && error.code() == 401) {
            _shouldRedirectToLogin.value = true
        }
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userResponse = userService.getUserProfile()
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()!!.user
                    _firstName.value = user.name
                    _lastName.value = user.surname
                    _userId.value = user.id
                    fetchUpcomingVisits()
                    fetchActiveCodes()
                } else if (userResponse.code() == 401) {
                    _shouldRedirectToLogin.value = true
                }
            } catch (e: Exception) {
                handleAuthError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchUpcomingVisits() {
        try {
            val visitsResponse = visitsService.getUpcomingVisits("true")
            if(visitsResponse.isSuccessful) {
                _upcomingVisits.value = visitsResponse.body()?.visits ?: emptyList()
            }
        } catch (e: retrofit2.HttpException) {
            Log.e("HomeViewModel", "HTTP Error ${e.code()}: ${e.message()}")
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching visits: $e")
        }
    }

    private suspend fun fetchActiveCodes() {
        try {
            val codesResponse = userService.getAllUserCodes()
            if (codesResponse.isSuccessful) {
                val codes = codesResponse.body()?.codes ?: emptyList()
                val prescriptions = codes.filter { it.codes.codeType == "PRESCRIPTION" }
                val referrals = codes.filter { it.codes.codeType == "REFERRAL" }

                _prescriptionCode.value = prescriptions.lastOrNull()?.codes?.code ?: ""
                _referralCode.value = referrals.lastOrNull()?.codes?.code ?: ""
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error fetching codes: $e")
        }
    }
}
