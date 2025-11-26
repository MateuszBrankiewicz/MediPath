package com.medipath.modules.shared.settings

import android.app.Application
import android.content.Intent
import android.os.Process.killProcess
import android.os.Process.myPid
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.SplashActivity
import com.medipath.core.models.UserSettingsRequest
import com.medipath.core.models.UserSettingsResponse
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.utils.LocaleHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import okio.IOException

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val settingsService = RetrofitInstance.settingsService

    private val _language = MutableStateFlow("PL")
    val language: StateFlow<String> = _language.asStateFlow()
    
    private var initialLanguage: String = "PL"

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

    private val context = getApplication<Application>()

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
                        initialLanguage = s.language
                        _systemNotifications.value = s.systemNotifications
                        _userNotifications.value = s.userNotifications
                        _lastPanel.value = s.lastPanel
                    }
                } else {
                    _error.value = context.getString(R.string.error_load_settings)
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
                    _error.value = errorBody ?: context.getString(R.string.error_deactivate)
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
                    LocaleHelper.setLocale(context, _language.value)
                    _updateSuccess.value = true
                    if (_language.value != initialLanguage) {
                        delay(500)
                        restartApp()
                    }
                } else {
                    _error.value = context.getString(R.string.error_update_settings)
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
    
    private fun restartApp() {
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        killProcess(myPid())
    }
}
