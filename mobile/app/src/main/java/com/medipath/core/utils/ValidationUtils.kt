package com.medipath.core.utils

import android.content.Context
import android.util.Patterns
import com.medipath.R
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

object ValidationUtils {

    fun validateName(name: String): Int? {
        return when {
            name.isBlank() -> R.string.error_name_required
            name.length < 2 -> R.string.error_name_too_short
            name.first().isLowerCase() -> R.string.error_name_must_start_capital
            !name.all { it.isLetter() || it.isWhitespace() } -> R.string.error_name_only_letters
            else -> null
        }
    }

    fun validateSurname(surname: String): Int? {
        return when {
            surname.isBlank() -> R.string.error_surname_required
            surname.length < 2 -> R.string.error_surname_too_short
            surname.first().isLowerCase() -> R.string.error_surname_must_start_capital
            !surname.all { it.isLetter() || it.isWhitespace() } -> R.string.error_surname_only_letters
            else -> null
        }
    }

    fun validateGovernmentId(id: String): Int? {
        return when {
            id.isBlank() -> R.string.error_govid_required
            id.length != 11 -> R.string.error_govid_length
            !id.all { it.isDigit() } -> R.string.error_govid_only_digits
            else -> null
        }
    }

    fun validateBirthDate(date: String, context: Context? = null): Int? {
        if (date.isBlank()) return R.string.error_birth_date_required

        val pattern = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$")
        if (!pattern.matcher(date).matches()) {
            return R.string.error_birth_date_format
        }

        try {
            val locale = context?.let { LocaleHelper.getLocale(it) } ?: Locale.getDefault()
            val sdf = SimpleDateFormat("dd-MM-yyyy", locale)
            sdf.isLenient = false
            val parsedDate = sdf.parse(date)
            val currentDate = System.currentTimeMillis()

            if (parsedDate == null || parsedDate.time > currentDate) {
                return R.string.error_birth_date_invalid
            }
        } catch (_: Exception) {
            return R.string.error_birth_date_invalid
        }

        return null
    }

    fun validatePostalCode(code: String): Int? {
        return when {
            code.isBlank() -> R.string.error_postal_code_required
            !Pattern.compile("^\\d{2}-\\d{3}$").matcher(code).matches() ->
                R.string.error_postal_code_format
            else -> null
        }
    }

    fun validatePhoneNumber(phone: String): Int? {
        return when {
            phone.isBlank() -> R.string.error_phone_required
            phone.length < 9 -> R.string.error_phone_too_short
            !phone.all { it.isDigit() || it == '+' || it == ' ' || it == '-' } ->
                R.string.error_phone_invalid
            else -> null
        }
    }

    fun validateEmail(email: String): Int? {
        return when {
            email.isBlank() -> R.string.error_email_required
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                R.string.error_email_invalid
            else -> null
        }
    }

    fun validatePassword(password: String): Int? {
        return when {
            password.isBlank() -> R.string.error_password_required
            password.length < 6 -> R.string.error_password_too_short
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): Int? {
        return when {
            confirmPassword.isBlank() -> R.string.error_confirm_password_required
            password != confirmPassword -> R.string.error_passwords_not_match
            else -> null
        }
    }

    fun validateCity(city: String): Int? {
        return when {
            city.isBlank() -> R.string.error_city_required
            else -> null
        }
    }

    fun validateProvince(province: String): Int? {
        return when {
            province.isBlank() -> R.string.error_province_required
            else -> null
        }
    }

    fun validateStreet(street: String): Int? {
        return when {
            street.isBlank() -> R.string.error_street_required
            !street.all { it.isLetter() } -> R.string.error_street_only_letters
            else -> null
        }
    }

    fun validateNumber(number: String): Int? {
        return when {
            number.isBlank() -> R.string.error_number_required
            !number.all { it.isDigit() } -> R.string.error_number_only_digits
            else -> null
        }
    }
}
