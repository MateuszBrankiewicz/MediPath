package com.medipath.modules.shared.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.UserSettingsRequest
import com.medipath.core.models.UserSettingsResponse
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val settingsService = RetrofitInstance.settingsService

    private val _language = MutableStateFlow("PL")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _systemNotifications = MutableStateFlow(true)
    val systemNotifications: StateFlow<Boolean> = _systemNotifications.asStateFlow()

    private val _userNotifications = MutableStateFlow(true)
    val userNotifications: StateFlow<Boolean> = _userNotifications.asStateFlow()

    private val _lastPanel = MutableStateFlow(1)
    val lastPanel: StateFlow<Int> = _lastPanel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _deactivateSuccess = MutableStateFlow(false)
    val deactivateSuccess: StateFlow<Boolean> = _deactivateSuccess.asStateFlow()

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun setSystemNotifications(enabled: Boolean) {
        _systemNotifications.value = enabled
    }

    fun setUserNotifications(enabled: Boolean) {
        _userNotifications.value = enabled
    }

    fun fetchSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val resp = settingsService.getSettings()
                if (resp.isSuccessful) {
                    val body: UserSettingsResponse? = resp.body()
                    body?.settings?.let { s ->
                        _language.value = s.language
                        _systemNotifications.value = s.systemNotifications
                        _userNotifications.value = s.userNotifications
                        _lastPanel.value = s.lastPanel
                    }
                } else {
                    _error.value = "Failed to load settings: ${resp.code()}"
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error fetching settings", e)
                _error.value = "${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun deactivateAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = settingsService.deactivateAccount()
                if (response.isSuccessful) {
                    _deactivateSuccess.value = true
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = errorBody ?: "Failed to deactivate account: ${response.code()}"
                    Log.e("ProfileViewModel", "Deactivate failed: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("ProfileViewModel", "Deactivate exception", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _updateSuccess.value = false
            try {
                val request = UserSettingsRequest(
                    language = _language.value,
                    systemNotifications = _systemNotifications.value,
                    userNotifications = _userNotifications.value
                )
                val resp = settingsService.updateSettings(request)
                if (resp.isSuccessful) {
                    _updateSuccess.value = true
                } else {
                    _error.value = "Failed to update settings: ${resp.code()}"
                }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error updating settings", e)
                _error.value = "${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
