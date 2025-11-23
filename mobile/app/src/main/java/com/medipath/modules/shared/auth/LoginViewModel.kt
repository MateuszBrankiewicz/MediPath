package com.medipath.modules.shared.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import com.medipath.R
import com.medipath.core.models.LoginRequest
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.utils.ValidationUtils
import com.medipath.core.utils.LocaleHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException


class LoginViewModel(
    application: Application
): AndroidViewModel(application) {
    private val authService = RetrofitInstance.authService
    private val settingsService = RetrofitInstance.settingsService

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val context = getApplication<Application>()

    private fun validateAndGetString(validationFunc: () -> Int?): String? {
        return validationFunc()?.let { context.getString(it) }
    }

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
        if (_emailError.value != null) {
            validateEmail()
        }
    }

    fun onPasswordChanged(newPass: String) {
        _password.value = newPass
        if (_passwordError.value != null) {
            validatePassword()
        }
    }

    fun validateEmail() {
        _emailError.value = validateAndGetString { ValidationUtils.validateEmail(_email.value) }
    }

    fun validatePassword() {
        _passwordError.value = validateAndGetString { ValidationUtils.validatePassword(_password.value) }
    }
    fun loginUser() {
        val request = LoginRequest(
            email = email.value,
            password = password.value
        )

        viewModelScope.launch {
            _loginError.value = null
            _loginSuccess.value = false
            _isLoading.value = true
            try {
                val response = authService.loginUser(request)
                if (response.isSuccessful) {
                    try {
                        val settingsResponse = settingsService.getSettings()
                        if (settingsResponse.isSuccessful) {
                            val language = settingsResponse.body()?.settings?.language
                            if (language != null) {
                                LocaleHelper.setLocale(context, language)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LoginViewModel", "Failed to fetch settings", e)
                    }
                    _loginSuccess.value = true
                } else {
                    when (response.code()) {
                        400 -> _loginError.value =
                            context.getString(R.string.please_fill_in_all_required_fields)
                        401 -> _loginError.value =
                            context.getString(R.string.invalid_email_or_password)
                        else -> _loginError.value =
                            context.getString(R.string.an_unknown_error_occurred_please_try_again_later)
                    }
                }
            } catch (_: IOException) {
                _loginError.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _loginError.value = context.getString(R.string.unknown_error)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _loginError.value = null
        _loginSuccess.value = false
    }
}