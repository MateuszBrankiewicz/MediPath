package com.medipath.modules.shared.auth.ui

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
import android.content.Intent
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.shared.components.AuthTextField
import com.medipath.modules.shared.auth.RegisterViewModel
import androidx.compose.runtime.getValue
import com.medipath.core.models.RegisterRequest
import com.medipath.modules.shared.components.SearchableCityDropdown
import com.medipath.modules.shared.components.SearchableProvinceDropdown
import com.medipath.core.utils.ValidationUtils
import android.widget.Toast
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel


class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediPathTheme { RegisterScreen(
                onSignInClick = {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onRegistrationSuccess = {
                    Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            ) }
        }
    }
}


@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onSignInClick: () -> Unit = {},
    onRegistrationSuccess: () -> Unit = {}
) {
    val registrationError by viewModel.registrationError
    val registrationSuccess by viewModel.registrationSuccess

    val name by viewModel.name
    val surname by viewModel.surname
    val governmentId by viewModel.governmentId
    val birthDate by viewModel.birthDate
    val number by viewModel.number
    val street by viewModel.street
    val postalCode by viewModel.postalCode
    val phoneNumber by viewModel.phoneNumber
    val email by viewModel.email
    val password by viewModel.password
    val confirmPassword by viewModel.confirmPassword
    val city by viewModel.city
    val province by viewModel.province
    val isChecked by viewModel.isChecked
    val isFormValid by viewModel.isFormValid

    val nameError by viewModel.nameError
    val surnameError by viewModel.surnameError
    val governmentIdError by viewModel.governmentIdError
    val birthDateError by viewModel.birthDateError
    val numberError by viewModel.numberError
    val streetError by viewModel.streetError
    val postalCodeError by viewModel.postalCodeError
    val phoneNumberError by viewModel.phoneNumberError
    val emailError by viewModel.emailError
    val passwordError by viewModel.passwordError
    val confirmPasswordError by viewModel.confirmPasswordError
    val cityError by viewModel.cityError
    val provinceError by viewModel.provinceError

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            onRegistrationSuccess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(WindowInsets.navigationBars.asPaddingValues()).background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal = 30.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(90.dp).padding(top = 40.dp))
        Spacer(modifier = Modifier.height(60.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text("Take care of your health.", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(250.dp))
            Text("Create an account", fontSize = 16.sp, fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AuthTextField(
                value = name,
                onValueChange = { viewModel.onNameChanged(it) },
                fieldText = "Name",
                hintText = "Enter your first name",
                errorMessage = nameError ?: "",
                onFocusLost = { viewModel.validateName() }
            )

            AuthTextField(
                value = surname,
                onValueChange = { viewModel.onSurnameChanged(it) },
                fieldText = "Surname",
                hintText = "Enter your last name",
                errorMessage = surnameError ?: "",
                onFocusLost = { viewModel.validateSurname() }
            )

            AuthTextField(
                value = governmentId,
                onValueChange = { viewModel.onGovernmentIdChanged(it) },
                fieldText = "Government ID",
                hintText = "Enter your PESEL number",
                errorMessage = governmentIdError ?: "",
                onFocusLost = { viewModel.validateGovernmentId() }
            )

            AuthTextField(
                value = birthDate,
                onValueChange = { viewModel.onBirthDateChanged(it) },
                fieldText = "Birth Date",
                hintText = "DD-MM-YYYY format",
                errorMessage = birthDateError ?: "",
                onFocusLost = { viewModel.validateBirthDate() }
            )

            SearchableProvinceDropdown(
                viewModel = viewModel,
                selectedProvince = province,
                onProvinceSelected = { viewModel.onProvinceChanged(it) },
                errorMessage = provinceError ?: "",
                onFocusLost = { viewModel.validateProvince() }
            )

            AuthTextField(
                value = postalCode,
                onValueChange = { viewModel.onPostalCodeChanged(it) },
                fieldText = "Postal Code",
                hintText = "XX-XXX format",
                errorMessage = postalCodeError ?: "",
                onFocusLost = { viewModel.validatePostalCode() }
            )

            SearchableCityDropdown(
                viewModel = viewModel,
                selectedCity = city,
                onCitySelected = { viewModel.onCityChanged(it) },
                errorMessage = cityError ?: "",
                onFocusLost = { viewModel.validateCity() }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AuthTextField(
                    value = number,
                    onValueChange = { viewModel.onNumberChanged(it) },
                    fieldText = "Number",
                    hintText = "Enter number",
                    modifier = Modifier.width(130.dp),
                    errorMessage = numberError ?: "",
                    onFocusLost = { viewModel.validateNumber() }
                )

                AuthTextField(
                    value = street,
                    onValueChange = { viewModel.onStreetChanged(it) },
                    fieldText = "Street",
                    hintText = "Enter street",
                    errorMessage = streetError ?: "",
                    onFocusLost = { viewModel.validateStreet() }
                )
            }

            AuthTextField(
                value = phoneNumber,
                onValueChange = { viewModel.onPhoneNumberChanged(it) },
                fieldText = "Phone Number",
                hintText = "Enter your phone number",
                keyboardType = KeyboardType.Phone,
                errorMessage = phoneNumberError ?: "",
                modifier = Modifier.testTag("phone_number"),
                onFocusLost = { viewModel.validatePhoneNumber() }
            )

            AuthTextField(
                value = email,
                onValueChange = { viewModel.onEmailChanged(it) },
                fieldText = "Email Address",
                hintText = "Enter your email address",
                keyboardType = KeyboardType.Email,
                errorMessage = emailError ?: "",
                modifier = Modifier.testTag("email_field"),
                onFocusLost = { viewModel.validateEmail() }
            )

            AuthTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                fieldText = "Password",
                hintText = "Enter your password",
                isPassword = true,
                errorMessage = passwordError ?: "",
                modifier = Modifier.testTag("password_field"),
                onFocusLost = { viewModel.validatePassword() }
            )

            AuthTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                fieldText = "Confirm Password",
                hintText = "Re-enter your password",
                isPassword = true,
                errorMessage = confirmPasswordError ?: "",
                modifier = Modifier.testTag("confirm_password_field"),
                onFocusLost = { viewModel.validateConfirmPassword() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { viewModel.onCheckedChanged(it) },
                modifier = Modifier.testTag("conditions_checkbox")
            )
            Text("Accept Terms & Conditions", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (registrationError != null) {
            Text(
                text = registrationError!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                viewModel.clearError()
                viewModel.registerUser()
            },
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)
        ) {
            Text(
                text = "SIGN UP",
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text("Already have an account? ", fontWeight = FontWeight.W400, fontSize = 14.sp)
            Text("Sign in", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable {
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
