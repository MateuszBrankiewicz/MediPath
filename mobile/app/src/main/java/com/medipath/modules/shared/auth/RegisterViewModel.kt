package com.medipath.modules.shared.auth

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import com.medipath.core.models.City
import com.medipath.core.models.RegisterRequest
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.AuthService
import com.medipath.core.services.LocationService
import com.medipath.core.utils.ValidationUtils
import retrofit2.HttpException

class RegisterViewModel(
    private val authService: AuthService = RetrofitInstance.authService,
    private val locationService: LocationService = RetrofitInstance.locationService
): ViewModel() {
    private val _cities = mutableStateOf<List<City>>(emptyList())
    val cities: State<List<City>> = _cities

    private val _provinces = mutableStateOf<List<String>>(emptyList())
    val provinces: State<List<String>> = _provinces

    private val _name = mutableStateOf("")
    val name: State<String> = _name
    private val _surname = mutableStateOf("")
    val surname: State<String> = _surname
    private val _governmentId = mutableStateOf("")
    val governmentId: State<String> = _governmentId
    private val _birthDate = mutableStateOf("")
    val birthDate: State<String> = _birthDate
    private val _number = mutableStateOf("")
    val number: State<String> = _number
    private val _street = mutableStateOf("")
    val street: State<String> = _street
    private val _postalCode = mutableStateOf("")
    val postalCode: State<String> = _postalCode
    private val _phoneNumber = mutableStateOf("")
    val phoneNumber: State<String> = _phoneNumber
    private val _email = mutableStateOf("")
    val email: State<String> = _email
    private val _password = mutableStateOf("")
    val password: State<String> = _password
    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword
    private val _city = mutableStateOf("")
    val city: State<String> = _city
    private val _province = mutableStateOf("")
    val province: State<String> = _province
    private val _isChecked = mutableStateOf(false)
    val isChecked: State<Boolean> = _isChecked

    private val _nameError = mutableStateOf<String?>(null)
    val nameError: State<String?> = _nameError
    private val _surnameError = mutableStateOf<String?>(null)
    val surnameError: State<String?> = _surnameError
    private val _governmentIdError = mutableStateOf<String?>(null)
    val governmentIdError: State<String?> = _governmentIdError
    private val _birthDateError = mutableStateOf<String?>(null)
    val birthDateError: State<String?> = _birthDateError
    private val _numberError = mutableStateOf<String?>(null)
    val numberError: State<String?> = _numberError
    private val _streetError = mutableStateOf<String?>(null)
    val streetError: State<String?> = _streetError
    private val _postalCodeError = mutableStateOf<String?>(null)
    val postalCodeError: State<String?> = _postalCodeError
    private val _phoneNumberError = mutableStateOf<String?>(null)
    val phoneNumberError: State<String?> = _phoneNumberError
    private val _emailError = mutableStateOf<String?>(null)
    val emailError: State<String?> = _emailError
    private val _passwordError = mutableStateOf<String?>(null)
    val passwordError: State<String?> = _passwordError
    private val _confirmPasswordError = mutableStateOf<String?>(null)
    val confirmPasswordError: State<String?> = _confirmPasswordError
    private val _cityError = mutableStateOf<String?>(null)
    val cityError: State<String?> = _cityError
    private val _provinceError = mutableStateOf<String?>(null)
    val provinceError: State<String?> = _provinceError

    private val _registrationError = mutableStateOf<String?>(null)
    val registrationError: State<String?> = _registrationError
    private val _registrationSuccess = mutableStateOf(false)
    val registrationSuccess: State<Boolean> = _registrationSuccess

    val isFormValid: State<Boolean> = derivedStateOf {
        name.value.isNotBlank() && surname.value.isNotBlank() &&
                governmentId.value.isNotBlank() && birthDate.value.isNotBlank() &&
                number.value.isNotBlank() && street.value.isNotBlank() &&
                postalCode.value.isNotBlank() && phoneNumber.value.isNotBlank() &&
                email.value.isNotBlank() && password.value.isNotBlank() &&
                confirmPassword.value.isNotBlank() && city.value.isNotBlank() &&
                province.value.isNotBlank() && isChecked.value &&
                nameError.value == null && surnameError.value == null &&
                governmentIdError.value == null && birthDateError.value == null &&
                numberError.value == null && streetError.value == null &&
                postalCodeError.value == null && phoneNumberError.value == null &&
                emailError.value == null && passwordError.value == null &&
                confirmPasswordError.value == null && cityError.value == null &&
                provinceError.value == null
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
                    Log.e("RegisterViewModel", "Error fetching cities: ${response.code()}")
                    _cities.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error fetching cities", e)
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
                    Log.e("RegisterViewModel", "Error fetching provinces: ${response.code()}")
                    _provinces.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error fetching provinces", e)
                _provinces.value = emptyList()
            }
        }
    }

    fun validateName() { _nameError.value = ValidationUtils.validateName(name.value).ifEmpty { null } }
    fun validateSurname() { _surnameError.value = ValidationUtils.validateSurname(surname.value).ifEmpty { null } }
    fun validateGovernmentId() { _governmentIdError.value = ValidationUtils.validateGovernmentId(governmentId.value).ifEmpty { null } }
    fun validateBirthDate() { _birthDateError.value = ValidationUtils.validateBirthDate(birthDate.value).ifEmpty { null } }
    fun validateNumber() { _numberError.value = ValidationUtils.validateNumber(number.value).ifEmpty { null } }
    fun validateStreet() { _streetError.value = ValidationUtils.validateStreet(street.value).ifEmpty { null } }
    fun validatePostalCode() { _postalCodeError.value = ValidationUtils.validatePostalCode(postalCode.value).ifEmpty { null } }
    fun validatePhoneNumber() { _phoneNumberError.value = ValidationUtils.validatePhoneNumber(phoneNumber.value).ifEmpty { null } }
    fun validateEmail() { _emailError.value = ValidationUtils.validateEmail(email.value).ifEmpty { null } }
    fun validatePassword() { _passwordError.value = ValidationUtils.validatePassword(password.value).ifEmpty { null } }
    fun validateConfirmPassword() { _confirmPasswordError.value = ValidationUtils.validateConfirmPassword(password.value, confirmPassword.value).ifEmpty { null } }
    fun validateCity() { _cityError.value = ValidationUtils.validateCity(city.value).ifEmpty { null } }
    fun validateProvince() { _provinceError.value = ValidationUtils.validateProvince(province.value).ifEmpty { null } }

    fun onNameChanged(value: String) { _name.value = value; if (_nameError.value != null) validateName() }
    fun onSurnameChanged(value: String) { _surname.value = value; if (_surnameError.value != null) validateSurname() }
    fun onGovernmentIdChanged(value: String) { _governmentId.value = value; if (_governmentIdError.value != null) validateGovernmentId() }
    fun onBirthDateChanged(value: String) { _birthDate.value = value; if (_birthDateError.value != null) validateBirthDate() }
    fun onNumberChanged(value: String) { _number.value = value; if (_numberError.value != null) validateNumber() }
    fun onStreetChanged(value: String) { _street.value = value; if (_streetError.value != null) validateStreet() }
    fun onPostalCodeChanged(value: String) { _postalCode.value = value; if (_postalCodeError.value != null) validatePostalCode() }
    fun onPhoneNumberChanged(value: String) { _phoneNumber.value = value; if (_phoneNumberError.value != null) validatePhoneNumber() }
    fun onEmailChanged(value: String) { _email.value = value; if (_emailError.value != null) validateEmail() }
    fun onPasswordChanged(value: String) { _password.value = value; if (_passwordError.value != null) validatePassword(); if (confirmPassword.value.isNotEmpty()) validateConfirmPassword() }
    fun onConfirmPasswordChanged(value: String) { _confirmPassword.value = value; if (_confirmPasswordError.value != null) validateConfirmPassword() }
    fun onCityChanged(value: String) { _city.value = value; if (_cityError.value != null) validateCity() }
    fun onProvinceChanged(value: String) { _province.value = value; if (_provinceError.value != null) validateProvince() }
    fun onCheckedChanged(value: Boolean) { _isChecked.value = value }

    fun registerUser() {
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
                    _registrationSuccess.value = true
                } else {
                    when (response.code()) {
                        400 -> {
                            _registrationError.value = "Please fill in all required fields."
                        }
                        409 -> {
                            _registrationError.value = "An account with this email or GovID already exists."
                        }
                        else -> {
                            _registrationError.value = "An unknown error occurred. Please try again later."
                        }
                    }
                }
            } catch (e: Exception) {
                _registrationError.value = e.message ?: "Registration failed"
            }
        }
    }

    fun clearError() {
        _registrationError.value = null
        _registrationSuccess.value = false
    }
}