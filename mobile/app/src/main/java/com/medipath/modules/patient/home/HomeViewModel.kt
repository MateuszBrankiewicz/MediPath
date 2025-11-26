package com.medipath.modules.patient.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import com.medipath.R
import com.medipath.core.models.Visit
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.utils.RoleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val userService = RetrofitInstance.userService
    private val visitsService = RetrofitInstance.visitsService
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

    private val _roleCode = MutableStateFlow(1)
    val roleCode: StateFlow<Int> = _roleCode.asStateFlow()

    private val _canBeDoctor = MutableStateFlow(false)
    val canBeDoctor: StateFlow<Boolean> = _canBeDoctor.asStateFlow()

    private val _prescriptionCode = MutableStateFlow("")
    val prescriptionCode: StateFlow<String> = _prescriptionCode.asStateFlow()

    private val _referralCode = MutableStateFlow("")
    val referralCode: StateFlow<String> = _referralCode.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    private val context = getApplication<Application>()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _shouldRedirectToLogin.value = false
            try {
                val userResponse = userService.getUserProfile()
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()!!.user
                    _firstName.value = user.name
                    _lastName.value = user.surname
                    _userId.value = user.id
                    _roleCode.value = user.roleCode
                    _canBeDoctor.value = RoleManager.canBeDoctor(user.roleCode)
                    fetchUpcomingVisits()
                    fetchActiveCodes()
                } else if (userResponse.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = context.getString(R.string.error_load_profile)
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

    private suspend fun fetchUpcomingVisits() {
        try {
            val visitsResponse = visitsService.getUpcomingVisits("true")
            if(visitsResponse.isSuccessful) {
                _upcomingVisits.value = visitsResponse.body()?.visits ?: emptyList()
            } else {
                _error.value = context.getString(R.string.error_load_upcoming_visits)
            }
        } catch (_: IOException) {
            _error.value = context.getString(R.string.error_connection)
        } catch (_: Exception) {
            _error.value = context.getString(R.string.unknown_error)
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
            } else {
                _error.value = context.getString(R.string.error_load_codes)
            }
        } catch (_: IOException) {
            _error.value = context.getString(R.string.error_connection)
        } catch (_: Exception) {
            _error.value = context.getString(R.string.unknown_error)
        }
    }

    fun clearError() {
        _error.value = null
    }
}
