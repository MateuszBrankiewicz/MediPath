package com.medipath.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

object ValidationUtils {

    fun validateName(name: String): String {
        return when {
            name.isBlank() -> "Name is required"
            name.length < 2 -> "Name must be at least 2 characters long"
            name.first().isLowerCase() -> "Name must start with a capital letter"
            !name.all { it.isLetter() || it.isWhitespace() } -> "Name can only contain letters"
            else -> ""
        }
    }

    fun validateSurname(surname: String): String {
        return when {
            surname.isBlank() -> "Surname is required"
            surname.length < 2 -> "Surname must be at least 2 characters long"
            surname.first().isLowerCase() -> "Surname must start with a capital letter"
            !surname.all { it.isLetter() || it.isWhitespace() } -> "Surname can only contain letters"
            else -> ""
        }
    }

    fun validateGovernmentId(id: String): String {
        return when {
            id.isBlank() -> "PESEL is required"
            id.length != 11 -> "PESEL must be exactly 11 digits long"
            !id.all { it.isDigit() } -> "PESEL can only contain digits"
            else -> ""
        }
    }

    fun validateBirthDate(date: String): String {
        if (date.isBlank()) return "Birth date is required"

        val pattern = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$")
        if (!pattern.matcher(date).matches()) {
            return "Date must be in DD-MM-YYYY format"
        }

        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            sdf.isLenient = false
            val parsedDate = sdf.parse(date)
            val currentDate = System.currentTimeMillis()

            if (parsedDate == null || parsedDate.time > currentDate) {
                return "Invalid date"
            }
        } catch (e: Exception) {
            return "Invalid date"
        }

        return ""
    }

    fun validatePostalCode(code: String): String {
        return when {
            code.isBlank() -> "Postal code is required"
            !Pattern.compile("^\\d{2}-\\d{3}$").matcher(code).matches() ->
                "Postal code must be in XX-XXX format"
            else -> ""
        }
    }

    fun validatePhoneNumber(phone: String): String {
        return when {
            phone.isBlank() -> "Phone number is required"
            phone.length < 9 -> "Phone number is too short"
            !phone.all { it.isDigit() || it == '+' || it == ' ' || it == '-' } ->
                "Invalid phone number format"
            else -> ""
        }
    }

    fun validateEmail(email: String): String {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Invalid email format"
            else -> ""
        }
    }

    fun validatePassword(password: String): String {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters long"
            else -> ""
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String {
        return when {
            confirmPassword.isBlank() -> "Password confirmation is required"
            password != confirmPassword -> "Passwords do not match"
            else -> ""
        }
    }

    fun validateCity(city: String): String {
        return when {
            city.isBlank() -> "City is required"
            else -> ""
        }
    }

    fun validateProvince(province: String): String {
        return when {
            province.isBlank() -> "Province is required"
            else -> ""
        }
    }

    fun validateStreet(street: String): String {
        return when {
            street.isBlank() -> "Street is required"
            !street.all { it.isLetter() } -> "Street can only contain letters"
            else -> ""
        }
    }

    fun validateNumber(number: String): String {
        return when {
            number.isBlank() -> "Number is required"
            !number.all { it.isDigit() } -> "Number can only contain digits"
            else -> ""
        }
    }
}
