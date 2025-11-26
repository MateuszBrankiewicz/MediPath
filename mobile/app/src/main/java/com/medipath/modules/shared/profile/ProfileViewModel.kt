package com.medipath.modules.shared.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.ResetPasswordRequest
import com.medipath.core.models.UserUpdateRequest
import com.medipath.core.models.UserProfileResponse
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.utils.ValidationUtils
import com.medipath.core.utils.RoleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {
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

    private val _rating = MutableStateFlow(0.0)
    val rating: StateFlow<Double> = _rating.asStateFlow()

    private val _numOfRatings = MutableStateFlow(0)
    val numOfRatings: StateFlow<Int> = _numOfRatings.asStateFlow()

    private val _roleCode = MutableStateFlow(1)
    val roleCode: StateFlow<Int> = _roleCode.asStateFlow()

    private val _canSwitchRole = MutableStateFlow(false)
    val canSwitchRole: StateFlow<Boolean> = _canSwitchRole.asStateFlow()

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

    private val context = getApplication<Application>()

    private fun validateAndGetString(validationFunc: () -> Int?): String? {
        return validationFunc()?.let { resId ->
            context.getString(resId)
        }
    }
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
                        _rating.value = u.rating
                        _numOfRatings.value = u.numOfRatings
                        _roleCode.value = u.roleCode
                        _canSwitchRole.value = RoleManager.canBeDoctor(u.roleCode)
                    }
                } else if (resp.code() == 401) {
                    _error.value = "401"
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

    fun updateProfile() {
        _nameError.value = validateAndGetString { ValidationUtils.validateName(_name.value) }
        _surnameError.value = validateAndGetString { ValidationUtils.validateSurname(_surname.value) }
        _phoneError.value = validateAndGetString { ValidationUtils.validatePhoneNumber(_phoneNumber.value) }
        _cityError.value = validateAndGetString { ValidationUtils.validateCity(_city.value) }
        _provinceError.value = validateAndGetString { ValidationUtils.validateProvince(_province.value) }
        _streetError.value = validateAndGetString { ValidationUtils.validateStreet(_street.value) }
        _numberError.value = validateAndGetString { ValidationUtils.validateNumber(_number.value) }
        _postalCodeError.value = validateAndGetString { ValidationUtils.validatePostalCode(_postalCode.value) }

        if (listOf(_nameError.value, _surnameError.value, _phoneError.value, _cityError.value,
                _provinceError.value, _streetError.value, _numberError.value, _postalCodeError.value)
                .any { it != null }) {
            _error.value = context.getString(R.string.please_fix_validation_errors)
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
                    _error.value = context.getString(R.string.error_update_profile)
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

    fun resetPassword(currentPassword: String, newPassword: String) {
        _currentPasswordError.value = validateAndGetString { ValidationUtils.validatePassword(currentPassword) }
        _newPasswordError.value = validateAndGetString { ValidationUtils.validatePassword(newPassword) }

        if (_currentPasswordError.value != null || _newPasswordError.value != null) {
            _error.value = context.getString(R.string.please_fix_password_validation_errors)
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
                    _error.value = context.getString(R.string.current_password_is_incorrect)
                } else if (resp.code() == 503) {
                    _error.value = context.getString(R.string.mail_service_error)
                } else {
                    _error.value = context.getString(R.string.error_reset_password)
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

    fun setName(v: String) {
        _name.value = v
        if (_nameError.value != null) validateName()
    }
    fun validateName() { _nameError.value = validateAndGetString { ValidationUtils.validateName(_name.value) } }

    fun setSurname(v: String) {
        _surname.value = v
        if (_surnameError.value != null) validateSurname()
    }
    fun validateSurname() { _surnameError.value = validateAndGetString { ValidationUtils.validateSurname(_surname.value) } }

    fun setPhoneNumber(v: String) {
        _phoneNumber.value = v
        if (_phoneError.value != null) validatePhoneNumber()
    }
    fun validatePhoneNumber() { _phoneError.value = validateAndGetString { ValidationUtils.validatePhoneNumber(_phoneNumber.value) } }

    fun setCity(v: String) {
        _city.value = v
        if (_cityError.value != null) validateCity()
    }
    fun validateCity() { _cityError.value = validateAndGetString { ValidationUtils.validateCity(_city.value) } }

    fun setProvince(v: String) {
        _province.value = v
        if (_provinceError.value != null) validateProvince()
    }
    fun validateProvince() { _provinceError.value = validateAndGetString { ValidationUtils.validateProvince(_province.value) } }

    fun setStreet(v: String) {
        _street.value = v
        if (_streetError.value != null) validateStreet()
    }
    fun validateStreet() { _streetError.value = validateAndGetString { ValidationUtils.validateStreet(_street.value) } }
    fun setNumber(v: String) {
        _number.value = v
        if (_numberError.value != null) validateNumber()
    }
    fun validateNumber() { _numberError.value = validateAndGetString { ValidationUtils.validateNumber(_number.value) } }

    fun setPostalCode(v: String) {
        _postalCode.value = v
        if (_postalCodeError.value != null) validatePostalCode()
    }
    fun validatePostalCode() { _postalCodeError.value = validateAndGetString { ValidationUtils.validatePostalCode(_postalCode.value) } }

    fun setPfpImage(v: String) {
        _pfpImage.value = v
    }

    fun validateCurrentPassword(password: String) {
        _currentPasswordError.value = validateAndGetString { ValidationUtils.validatePassword(password) }
    }

    fun validateNewPassword(password: String) {
        _newPasswordError.value = validateAndGetString { ValidationUtils.validatePassword(password) }
    }
}
