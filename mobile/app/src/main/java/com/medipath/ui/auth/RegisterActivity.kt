package com.medipath.ui.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.ui.splash.SplashActivity
import android.content.Intent
import com.medipath.ui.theme.MediPathTheme
import com.medipath.ui.components.AuthTextField
import com.medipath.viewmodels.RegisterViewModel
import androidx.compose.runtime.getValue
import com.medipath.data.models.RegisterRequest
import com.medipath.ui.components.SearchableCityDropdown
import com.medipath.ui.components.SearchableProvinceDropdown
import com.medipath.utils.ValidationUtils
import android.widget.Toast
import androidx.compose.ui.platform.testTag


class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediPathTheme { RegisterScreen(
                onSignInClick = {
                    val intent = Intent(this, SplashActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onRegistrationSuccess = {
                    Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show()
                    //przejscie do logowania
                }
            ) }
        }
    }
}



@Composable
fun RegisterScreen(viewModel: RegisterViewModel = remember { RegisterViewModel() }, onSignInClick: () -> Unit = {}, onRegistrationSuccess: () -> Unit = {}) {

    var city by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("") }
    val registrationError by viewModel.registrationError
    val registrationSuccess by viewModel.registrationSuccess

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            onRegistrationSuccess()
        }
    }

    var isChecked by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var governmentId by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf("") }
    var surnameError by remember { mutableStateOf("") }
    var governmentIdError by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf("") }
    var numberError by remember { mutableStateOf("") }
    var streetError by remember { mutableStateOf("") }
    var postalCodeError by remember { mutableStateOf("") }
    var phoneNumberError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var cityError by remember { mutableStateOf("") }
    var provinceError by remember { mutableStateOf("") }

    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() &&
            surname.isNotBlank() &&
            governmentId.isNotBlank() &&
            birthDate.isNotBlank() &&
            number.isNotBlank() &&
            street.isNotBlank() &&
            postalCode.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            email.isNotBlank() &&
            password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            city.isNotBlank() &&
            province.isNotBlank() &&
            isChecked
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(90.dp))
        Spacer(modifier = Modifier.height(60.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Take care of your health.", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(250.dp))
            Text("Create an account", fontSize = 16.sp, fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AuthTextField(name, {
                name = it
                nameError = ValidationUtils.validateName(it)
            },
                "Name", "Enter your first name", errorMessage = nameError,
                onFocusLost = {
                    nameError = ValidationUtils.validateName(name)
                }
            )

            AuthTextField(surname, {
                surname = it
                surnameError = ValidationUtils.validateSurname(it)
            }, "Surname", "Enter your last name", errorMessage = surnameError,
                onFocusLost = {
                    surnameError = ValidationUtils.validateSurname(surname)
                }
            )

            AuthTextField(governmentId, {
                governmentId = it
                governmentIdError = ValidationUtils.validateGovernmentId(it)
            }, "Government ID", "Enter your PESEL number", errorMessage = governmentIdError,
                onFocusLost = {
                    governmentIdError = ValidationUtils.validateGovernmentId(governmentId)
                }
                )

            AuthTextField(birthDate, {
                birthDate = it
                birthDateError = ValidationUtils.validateBirthDate(it)
            }, "Birth Date", "DD-MM-YYYY format", errorMessage = birthDateError,
                onFocusLost = {
                    birthDateError = ValidationUtils.validateBirthDate(birthDate)
                }
                )

            SearchableProvinceDropdown(
                viewModel = viewModel,
                selectedProvince = province,
                onProvinceSelected = {
                    province = it
                    provinceError = ValidationUtils.validateProvince(it)
                },
                errorMessage = provinceError,
                onFocusLost = {
                    provinceError = ValidationUtils.validateProvince(province)
                }
            )

            AuthTextField(postalCode, {
                postalCode = it
                postalCodeError = ValidationUtils.validatePostalCode(it)
            }, "Postal Code", "XX-XXX format", errorMessage = postalCodeError,
                onFocusLost = {
                    postalCodeError = ValidationUtils.validatePostalCode(postalCode)
                }
            )

            SearchableCityDropdown(
                viewModel = viewModel,
                selectedCity = city,
                onCitySelected = {
                    city = it
                    cityError = ValidationUtils.validateCity(it)
                },
                errorMessage = cityError,
                onFocusLost = {
                    cityError = ValidationUtils.validateCity(city)
                }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AuthTextField(number, {
                    number = it
                    numberError = ValidationUtils.validateNumber(it)
                }, "Number", "Enter number", modifier = Modifier.width(130.dp), errorMessage = numberError,
                    onFocusLost = {
                        numberError = ValidationUtils.validateNumber(number)
                    }
                )

                AuthTextField(street, {
                    street = it
                    streetError = ValidationUtils.validateStreet(it)
                }, "Street", "Enter street", errorMessage = streetError,
                    onFocusLost = {
                        streetError = ValidationUtils.validateStreet(street)
                    }
                )
            }

            AuthTextField(phoneNumber, {
                phoneNumber = it
                phoneNumberError = ValidationUtils.validatePhoneNumber(it)
            }, "Phone Number", "Enter your phone number", keyboardType = KeyboardType.Phone, errorMessage = phoneNumberError,
                modifier = Modifier.testTag("phone_number"),
                onFocusLost = {
                    phoneNumberError = ValidationUtils.validatePhoneNumber(phoneNumber)
                }
            )

            AuthTextField(email, {
                email = it
                emailError = ValidationUtils.validateEmail(it)
            }, "Email Address", "Enter your email address", keyboardType = KeyboardType.Email, errorMessage = emailError,
                modifier = Modifier.testTag("email_field"),
                onFocusLost = {
                    emailError = ValidationUtils.validateEmail(email)
                }
            )

            AuthTextField(password, {
                password = it
                passwordError = ValidationUtils.validatePassword(it)
                if (confirmPassword.isNotEmpty()) {
                    confirmPasswordError = ValidationUtils.validateConfirmPassword(it, confirmPassword)
                }
            }, "Password", "Enter your password", isPassword = true, errorMessage = passwordError,
                modifier = Modifier.testTag("password_field"),
                onFocusLost = {
                    passwordError = ValidationUtils.validatePassword(password)
                }
            )

            AuthTextField(confirmPassword, {
                confirmPassword = it
                confirmPasswordError = ValidationUtils.validateConfirmPassword(password, it)
            }, "Confirm Password", "Re-enter your password", isPassword = true, errorMessage = confirmPasswordError,
                modifier = Modifier.testTag("confirm_password_field"),
                onFocusLost = {
                    confirmPasswordError = ValidationUtils.validateConfirmPassword(password, confirmPassword)
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(isChecked, { isChecked = it }, modifier = Modifier.testTag("conditions_checkbox"))
            Text("Accept Terms & Conditions", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (registrationError.isNotEmpty()) {
            Text(
                text = registrationError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                viewModel.clearError()
                val registerRequest = RegisterRequest(
                    name = name,
                    surname = surname,
                    email = email,
                    govID = governmentId,
                    birthDate = birthDate,
                    province = province,
                    city = city,
                    postalCode = postalCode,
                    phoneNumber = phoneNumber,
                    street = street,
                    number = number,
                    password = password
                )
                viewModel.registerUser(registerRequest)
            },
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Text(
                text = "SIGN UP",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Text("Already have an account? ", fontWeight = FontWeight.W400)
            Text("Sign in", fontWeight = FontWeight.Bold, modifier = Modifier.clickable {
                onSignInClick()
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MediPathTheme { RegisterScreen() }
}
