package com.medipath.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

object ValidationUtils {

    fun validateName(name: String): String {
        return when {
            name.isBlank() -> "Imię jest wymagane"
            name.length < 2 -> "Imię musi mieć co najmniej 2 znaki"
            name.first().isLowerCase() -> "Imię musi zaczynać się wielką literą"
            !name.all { it.isLetter() || it.isWhitespace() } -> "Imię może zawierać tylko litery"
            else -> ""
        }
    }

    fun validateSurname(surname: String): String {
        return when {
            surname.isBlank() -> "Nazwisko jest wymagane"
            surname.length < 2 -> "Nazwisko musi mieć co najmniej 2 znaki"
            surname.first().isLowerCase() -> "Nazwisko musi zaczynać się wielką literą"
            !surname.all { it.isLetter() || it.isWhitespace() } -> "Nazwisko może zawierać tylko litery"
            else -> ""
        }
    }

    fun validateGovernmentId(id: String): String {
        return when {
            id.isBlank() -> "PESEL jest wymagany"
            id.length != 11 -> "PESEL musi mieć dokładnie 11 cyfr"
            !id.all { it.isDigit() } -> "PESEL może zawierać tylko cyfry"
            else -> ""
        }
    }

    fun validateBirthDate(date: String): String {
        if (date.isBlank()) return "Data urodzenia jest wymagana"

        val pattern = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$")
        if (!pattern.matcher(date).matches()) {
            return "Data musi być w formacie DD-MM-YYYY"
        }

        try {
            val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            sdf.isLenient = false
            val parsedDate = sdf.parse(date)
            val currentDate = System.currentTimeMillis()

            if (parsedDate == null || parsedDate.time > currentDate) {
                return "Nieprawidłowa data"
            }
        } catch (e: Exception) {
            return "Nieprawidłowa data"
        }

        return ""
    }

    fun validatePostalCode(code: String): String {
        return when {
            code.isBlank() -> "Kod pocztowy jest wymagany"
            !Pattern.compile("^\\d{2}-\\d{3}$").matcher(code).matches() ->
                "Kod pocztowy musi być w formacie XX-XXX"
            else -> ""
        }
    }

    fun validatePhoneNumber(phone: String): String {
        return when {
            phone.isBlank() -> "Numer telefonu jest wymagany"
            phone.length < 9 -> "Numer telefonu jest za krótki"
            !phone.all { it.isDigit() || it == '+' || it == ' ' || it == '-' } ->
                "Nieprawidłowy format numeru telefonu"
            else -> ""
        }
    }

    fun validateEmail(email: String): String {
        return when {
            email.isBlank() -> "Email jest wymagany"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Nieprawidłowy format email"
            else -> ""
        }
    }

    fun validatePassword(password: String): String {
        return when {
            password.isBlank() -> "Hasło jest wymagane"
            password.length < 6 -> "Hasło musi mieć co najmniej 6 znaków"
            else -> ""
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String {
        return when {
            confirmPassword.isBlank() -> "Potwierdzenie hasła jest wymagane"
            password != confirmPassword -> "Hasła nie są identyczne"
            else -> ""
        }
    }

    fun validateCity(city: String): String {
        return when {
            city.isBlank() -> "Miasto jest wymagane"
            else -> ""
        }
    }

    fun validateProvince(province: String): String {
        return when {
            province.isBlank() -> "Województwo jest wymagane"
            else -> ""
        }
    }

    fun validateStreet(street: String): String {
        return when {
            street.isBlank() -> "Ulica jest wymagana"
            !street.all { it.isLetter() } -> "Ulica może zawierać tylko litery"
            else -> ""
        }
    }

    fun validateNumber(number: String): String {
        return when {
            number.isBlank() -> "Numer jest wymagany"
            !number.all { it.isDigit() } -> "Numer może zawierać tylko cyfry"
            else -> ""
        }
    }
}
