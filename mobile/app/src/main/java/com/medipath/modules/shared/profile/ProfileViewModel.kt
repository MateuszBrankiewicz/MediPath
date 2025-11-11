package com.medipath.modules.shared.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.ResetPasswordRequest
import com.medipath.core.models.UserUpdateRequest
import com.medipath.core.models.UserProfileResponse
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val userService = RetrofitInstance.userService

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _surname = MutableStateFlow("")
    val surname: StateFlow<String> = _surname.asStateFlow()

    private val _birthDate = MutableStateFlow("")
    val birthDate: StateFlow<String> = _birthDate.asStateFlow()

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _govId = MutableStateFlow("")
    val govId: StateFlow<String> = _govId.asStateFlow()

    private val _city = MutableStateFlow("")
    val city: StateFlow<String> = _city.asStateFlow()

    private val _province = MutableStateFlow("")
    val province: StateFlow<String> = _province.asStateFlow()

    private val _street = MutableStateFlow("")
    val street: StateFlow<String> = _street.asStateFlow()

    private val _number = MutableStateFlow("")
    val number: StateFlow<String> = _number.asStateFlow()

    private val _postalCode = MutableStateFlow("")
    val postalCode: StateFlow<String> = _postalCode.asStateFlow()

    private val _pfpImage = MutableStateFlow("")
    val pfpImage: StateFlow<String> = _pfpImage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()

    private val _surnameError = MutableStateFlow<String?>(null)
    val surnameError: StateFlow<String?> = _surnameError.asStateFlow()

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError.asStateFlow()

    private val _cityError = MutableStateFlow<String?>(null)
    val cityError: StateFlow<String?> = _cityError.asStateFlow()

    private val _provinceError = MutableStateFlow<String?>(null)
    val provinceError: StateFlow<String?> = _provinceError.asStateFlow()

    private val _streetError = MutableStateFlow<String?>(null)
    val streetError: StateFlow<String?> = _streetError.asStateFlow()

    private val _numberError = MutableStateFlow<String?>(null)
    val numberError: StateFlow<String?> = _numberError.asStateFlow()

    private val _postalCodeError = MutableStateFlow<String?>(null)
    val postalCodeError: StateFlow<String?> = _postalCodeError.asStateFlow()

    private val _currentPasswordError = MutableStateFlow<String?>(null)
    val currentPasswordError: StateFlow<String?> = _currentPasswordError.asStateFlow()

    private val _newPasswordError = MutableStateFlow<String?>(null)
    val newPasswordError: StateFlow<String?> = _newPasswordError.asStateFlow()

    fun fetchProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val resp = userService.getUserProfile()
                if (resp.isSuccessful) {
                    val body: UserProfileResponse? = resp.body()
                    body?.user?.let { u ->
                        _name.value = u.name
                        _surname.value = u.surname
                        _birthDate.value = u.birthDate
                        _phoneNumber.value = u.phoneNumber
                        _govId.value = u.govId
                        _city.value = u.address.city
                        _province.value = u.address.province
                        _street.value = u.address.street
                        _number.value = u.address.number
                        _postalCode.value = u.address.postalCode
                        _pfpImage.value = u.pfpImage
                    }
                } else if (resp.code() == 401) {
                    _error.value = "401"
                } else {
                    _error.value = "Failed to load profile: ${resp.code()}"
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching profile", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile() {
        val nameValidation = ValidationUtils.validateName(_name.value)
        val surnameValidation = ValidationUtils.validateSurname(_surname.value)
        val phoneValidation = ValidationUtils.validatePhoneNumber(_phoneNumber.value)
        val cityValidation = ValidationUtils.validateCity(_city.value)
        val provinceValidation = ValidationUtils.validateProvince(_province.value)
        val streetValidation = ValidationUtils.validateStreet(_street.value)
        val numberValidation = ValidationUtils.validateNumber(_number.value)
        val postalCodeValidation = ValidationUtils.validatePostalCode(_postalCode.value)

        _nameError.value = nameValidation.ifEmpty { null }
        _surnameError.value = surnameValidation.ifEmpty { null }
        _phoneError.value = phoneValidation.ifEmpty { null }
        _cityError.value = cityValidation.ifEmpty { null }
        _provinceError.value = provinceValidation.ifEmpty { null }
        _streetError.value = streetValidation.ifEmpty { null }
        _numberError.value = numberValidation.ifEmpty { null }
        _postalCodeError.value = postalCodeValidation.ifEmpty { null }

        if (listOf(nameValidation, surnameValidation, phoneValidation, cityValidation,
                provinceValidation, streetValidation, numberValidation, postalCodeValidation)
                .any { it.isNotEmpty() }) {
            _error.value = "Please fix validation errors"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _updateSuccess.value = false
            try {
                val req = UserUpdateRequest(
                    city = _city.value,
                    name = _name.value,
                    surname = _surname.value,
                    province = _province.value,
                    postalCode = _postalCode.value,
                    number = _number.value,
                    street = _street.value,
                    phoneNumber = _phoneNumber.value,
                    pfpImage = _pfpImage.value.ifEmpty { null }
                )
                val resp = userService.updateProfile(req)
                if (resp.isSuccessful) {
                    _updateSuccess.value = true
                } else if (resp.code() == 401) {
                    _error.value = "401"
                } else {
                    _error.value = "Failed to update profile: ${resp.code()}"
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating profile", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetPassword(currentPassword: String, newPassword: String) {
        val currentPasswordValidation = ValidationUtils.validatePassword(currentPassword)
        val newPasswordValidation = ValidationUtils.validatePassword(newPassword)

        _currentPasswordError.value = currentPasswordValidation.ifEmpty { null }
        _newPasswordError.value = newPasswordValidation.ifEmpty { null }

        if (currentPasswordValidation.isNotEmpty() || newPasswordValidation.isNotEmpty()) {
            _error.value = "Please fix password validation errors"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _resetSuccess.value = false
            _currentPasswordError.value = null
            _newPasswordError.value = null
            try {
                val req = ResetPasswordRequest(currentPassword = currentPassword, newPassword = newPassword)
                val resp = userService.resetPassword(req)
                if (resp.isSuccessful) {
                    _resetSuccess.value = true
                } else if (resp.code() == 401) {
                    _error.value = "Current password is incorrect"
                } else if (resp.code() == 503) {
                    _error.value = "Mail service error"
                } else {
                    _error.value = "Failed to reset password: ${resp.code()}"
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error resetting password", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setName(v: String) {
        _name.value = v
        _nameError.value = ValidationUtils.validateName(v).ifEmpty { null }
    }
    fun setSurname(v: String) { 
        _surname.value = v
        _surnameError.value = ValidationUtils.validateSurname(v).ifEmpty { null }
    }
    fun setPhoneNumber(v: String) { 
        _phoneNumber.value = v
        _phoneError.value = ValidationUtils.validatePhoneNumber(v).ifEmpty { null }
    }
    fun setCity(v: String) { 
        _city.value = v
        _cityError.value = ValidationUtils.validateCity(v).ifEmpty { null }
    }
    fun setProvince(v: String) { 
        _province.value = v
        _provinceError.value = ValidationUtils.validateProvince(v).ifEmpty { null }
    }
    fun setStreet(v: String) { 
        _street.value = v
        _streetError.value = ValidationUtils.validateStreet(v).ifEmpty { null }
    }
    fun setNumber(v: String) { 
        _number.value = v
        _numberError.value = ValidationUtils.validateNumber(v).ifEmpty { null }
    }
    fun setPostalCode(v: String) { 
        _postalCode.value = v
        _postalCodeError.value = ValidationUtils.validatePostalCode(v).ifEmpty { null }
    }
    
    fun setPfpImage(v: String) {
        _pfpImage.value = v
    }
    
    fun validateCurrentPassword(password: String) {
        _currentPasswordError.value = ValidationUtils.validatePassword(password).ifEmpty { null }
    }
    
    fun validateNewPassword(password: String) {
        _newPasswordError.value = ValidationUtils.validatePassword(password).ifEmpty { null }
    }
}
