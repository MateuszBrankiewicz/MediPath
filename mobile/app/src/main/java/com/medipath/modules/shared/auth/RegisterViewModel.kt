package com.medipath.modules.shared.auth

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.medipath.R
import com.medipath.core.models.City
import com.medipath.core.models.RegisterRequest
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.utils.ValidationUtils
import com.medipath.core.utils.LocaleHelper
import com.medipath.core.models.UserSettingsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException
import java.util.Locale

class RegisterViewModel(
    application: Application
): AndroidViewModel(application) {
    private val authService = RetrofitInstance.authService
    private val locationService = RetrofitInstance.locationService
    private val settingsService = RetrofitInstance.settingsService
    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()
    private val _provinces = MutableStateFlow<List<String>>(emptyList())
    val provinces: StateFlow<List<String>> = _provinces.asStateFlow()
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    private val _surname = MutableStateFlow("")
    val surname: StateFlow<String> = _surname.asStateFlow()
    private val _governmentId = MutableStateFlow("")
    val governmentId: StateFlow<String> = _governmentId.asStateFlow()
    private val _birthDate = MutableStateFlow("")
    val birthDate: StateFlow<String> = _birthDate.asStateFlow()
    private val _number = MutableStateFlow("")
    val number: StateFlow<String> = _number.asStateFlow()
    private val _street = MutableStateFlow("")
    val street: StateFlow<String> = _street.asStateFlow()
    private val _postalCode = MutableStateFlow("")
    val postalCode: StateFlow<String> = _postalCode.asStateFlow()
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()
    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()
    private val _province = MutableStateFlow("")
    val province: StateFlow<String> = _province.asStateFlow()
    private val _isChecked = MutableStateFlow(false)
    val isChecked: StateFlow<Boolean> = _isChecked.asStateFlow()
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()
    private val _surnameError = MutableStateFlow<String?>(null)
    val surnameError: StateFlow<String?> = _surnameError.asStateFlow()
    private val _governmentIdError = MutableStateFlow<String?>(null)
    val governmentIdError: StateFlow<String?> = _governmentIdError.asStateFlow()
    private val _birthDateError = MutableStateFlow<String?>(null)
    val birthDateError: StateFlow<String?> = _birthDateError.asStateFlow()
    private val _numberError = MutableStateFlow<String?>(null)
    val numberError: StateFlow<String?> = _numberError.asStateFlow()
    private val _streetError = MutableStateFlow<String?>(null)
    val streetError: StateFlow<String?> = _streetError.asStateFlow()
    private val _postalCodeError = MutableStateFlow<String?>(null)
    val postalCodeError: StateFlow<String?> = _postalCodeError.asStateFlow()
    private val _phoneNumberError = MutableStateFlow<String?>(null)
    val phoneNumberError: StateFlow<String?> = _phoneNumberError.asStateFlow()
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()
    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()
    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()
    private val _cityError = MutableStateFlow<String?>(null)
    val cityError: StateFlow<String?> = _cityError.asStateFlow()
    private val _provinceError = MutableStateFlow<String?>(null)
    val provinceError: StateFlow<String?> = _provinceError.asStateFlow()
    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError.asStateFlow()
    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    private val context = getApplication<Application>()

    private fun validateAndGetString(validationFunc: () -> Int?): String? {
        return validationFunc()?.let { context.getString(it) }
    }

    init {
        fetchCities()
        fetchProvinces()
    }

    private fun fetchCities() {
        viewModelScope.launch {
            try {
                val response = locationService.getCities()
                if (response.isSuccessful) {
                    _cities.value = response.body() ?: emptyList()
                } else {
                    _cities.value = emptyList()
                }
            } catch (_: Exception) {
                _cities.value = emptyList()
            }
        }
    }

    private fun fetchProvinces() {
        viewModelScope.launch {
            try {
                val response = locationService.getProvinces()
                if (response.isSuccessful) {
                    _provinces.value = response.body() ?: emptyList()
                } else {
                    _provinces.value = emptyList()
                }
            } catch (_: Exception) {
                _provinces.value = emptyList()
            }
        }
    }

    fun validateName() { _nameError.value = validateAndGetString { ValidationUtils.validateName(_name.value) } }
    fun validateSurname() { _surnameError.value = validateAndGetString { ValidationUtils.validateSurname(_surname.value) } }
    fun validateGovernmentId() { _governmentIdError.value = validateAndGetString { ValidationUtils.validateGovernmentId(_governmentId.value) } }
    fun validateBirthDate() { _birthDateError.value = validateAndGetString { ValidationUtils.validateBirthDate(_birthDate.value) } }
    fun validateNumber() { _numberError.value = validateAndGetString { ValidationUtils.validateNumber(_number.value) } }
    fun validateStreet() { _streetError.value = validateAndGetString { ValidationUtils.validateStreet(_street.value) } }
    fun validatePostalCode() { _postalCodeError.value = validateAndGetString { ValidationUtils.validatePostalCode(_postalCode.value) } }
    fun validatePhoneNumber() { _phoneNumberError.value = validateAndGetString { ValidationUtils.validatePhoneNumber(_phoneNumber.value) } }
    fun validateEmail() { _emailError.value = validateAndGetString { ValidationUtils.validateEmail(_email.value) } }
    fun validatePassword() { _passwordError.value = validateAndGetString { ValidationUtils.validatePassword(_password.value) } }
    fun validateConfirmPassword() { _confirmPasswordError.value = validateAndGetString { ValidationUtils.validateConfirmPassword(_password.value, _confirmPassword.value) } }
    fun validateCity() { _cityError.value = validateAndGetString { ValidationUtils.validateCity(_city.value) } }
    fun validateProvince() { _provinceError.value = validateAndGetString { ValidationUtils.validateProvince(_province.value) } }

    fun onNameChanged(value: String) { _name.value = value; if (_nameError.value != null) validateName() }
    fun onSurnameChanged(value: String) { _surname.value = value; if (_surnameError.value != null) validateSurname() }
    fun onGovernmentIdChanged(value: String) { _governmentId.value = value; if (_governmentIdError.value != null) validateGovernmentId() }
    fun onBirthDateChanged(value: String) { _birthDate.value = value; if (_birthDateError.value != null) validateBirthDate() }
    fun onNumberChanged(value: String) { _number.value = value; if (_numberError.value != null) validateNumber() }
    fun onStreetChanged(value: String) { _street.value = value; if (_streetError.value != null) validateStreet() }
    fun onPostalCodeChanged(value: String) { _postalCode.value = value; if (_postalCodeError.value != null) validatePostalCode() }
    fun onPhoneNumberChanged(value: String) { _phoneNumber.value = value; if (_phoneNumberError.value != null) validatePhoneNumber() }
    fun onEmailChanged(value: String) { _email.value = value; if (_emailError.value != null) validateEmail() }

    fun onPasswordChanged(value: String) {
        _password.value = value
        if (_passwordError.value != null) validatePassword()
        if (_confirmPassword.value.isNotEmpty()) validateConfirmPassword()
    }

    fun onConfirmPasswordChanged(value: String) {
        _confirmPassword.value = value
        if (_confirmPasswordError.value != null) validateConfirmPassword()
    }

    fun onCityChanged(value: String) { _city.value = value; if (_cityError.value != null) validateCity() }
    fun onProvinceChanged(value: String) { _province.value = value; if (_provinceError.value != null) validateProvince() }
    fun onCheckedChanged(value: Boolean) { _isChecked.value = value }

    fun registerUser() {
        if (listOf(_nameError.value, _surnameError.value, _governmentIdError.value, _birthDateError.value,
                _numberError.value, _streetError.value, _postalCodeError.value, _phoneNumberError.value,
                _emailError.value, _passwordError.value, _confirmPasswordError.value, _cityError.value, _provinceError.value)
                .any { it != null } || !_isChecked.value) {
            _registrationError.value =
                context.getString(R.string.please_fix_validation_errors_and_accept_terms)
            return
        }

        val systemLanguage = Locale.getDefault().language
        val langToSend = if (systemLanguage.equals("pl", ignoreCase = true)) "PL" else "EN"

        val registerRequest = RegisterRequest(
            name = name.value,
            surname = surname.value,
            email = email.value,
            govID = governmentId.value,
            birthDate = birthDate.value,
            province = province.value,
            city = city.value,
            postalCode = postalCode.value,
            phoneNumber = phoneNumber.value,
            street = street.value,
            number = number.value,
            password = password.value
        )

        viewModelScope.launch {
            _registrationError.value = null
            _registrationSuccess.value = false

            try {
                val response = authService.registerUser(registerRequest)
                if (response.isSuccessful) {
                    try {
                        val settingsRequest = UserSettingsRequest(
                            language = langToSend,
                            systemNotifications = true,
                            userNotifications = true
                        )
                        val settingsResponse = settingsService.updateSettings(settingsRequest)
                        if (settingsResponse.isSuccessful) {
                            LocaleHelper.setLocale(context, langToSend)
                        }
                    } catch (e: Exception) {
                        Log.e("RegisterViewModel", "Failed to set initial language", e)
                    }
                    _registrationSuccess.value = true
                } else {
                    when (response.code()) {
                        400 -> {
                            _registrationError.value = context.getString(R.string.please_fill_in_all_required_fields)
                        }
                        409 -> {
                            _registrationError.value =
                                context.getString(R.string.an_account_with_this_email_or_govid_already_exists)
                        }
                        else -> {
                            _registrationError.value = context.getString(R.string.an_unknown_error_occurred_please_try_again_later)
                        }
                    }
                }
            } catch (_: IOException) {
                _registrationError.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _registrationError.value = context.getString(R.string.unknown_error)
            }
        }
    }

    fun clearError() {
        _registrationError.value = null
        _registrationSuccess.value = false
    }
}