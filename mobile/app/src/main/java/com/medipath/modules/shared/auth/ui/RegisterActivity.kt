package com.medipath.modules.shared.auth.ui

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.medipath.modules.shared.components.SearchableCityDropdown
import com.medipath.modules.shared.components.SearchableProvinceDropdown
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar


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
                    Toast.makeText(this,
                        getString(R.string.registration_successful_please_log_in), Toast.LENGTH_LONG).show()
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
    onSignInClick: () -> Unit = {},
    onRegistrationSuccess: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    val context = LocalContext.current

    val registrationError by viewModel.registrationError.collectAsState()
    val registrationSuccess by viewModel.registrationSuccess.collectAsState()

    val name by viewModel.name.collectAsState()
    val surname by viewModel.surname.collectAsState()
    val governmentId by viewModel.governmentId.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val number by viewModel.number.collectAsState()
    val street by viewModel.street.collectAsState()
    val postalCode by viewModel.postalCode.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val city by viewModel.city.collectAsState()
    val province by viewModel.province.collectAsState()
    val isChecked by viewModel.isChecked.collectAsState()

    val nameError by viewModel.nameError.collectAsState()
    val surnameError by viewModel.surnameError.collectAsState()
    val governmentIdError by viewModel.governmentIdError.collectAsState()
    val birthDateError by viewModel.birthDateError.collectAsState()
    val numberError by viewModel.numberError.collectAsState()
    val streetError by viewModel.streetError.collectAsState()
    val postalCodeError by viewModel.postalCodeError.collectAsState()
    val phoneNumberError by viewModel.phoneNumberError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val cityError by viewModel.cityError.collectAsState()
    val provinceError by viewModel.provinceError.collectAsState()

    val isFormValid by remember(
        name, surname, governmentId, birthDate, number, street,
        postalCode, phoneNumber, email, password, confirmPassword,
        city, province, isChecked
    ) {
        derivedStateOf {
            name.isNotBlank() && surname.isNotBlank() &&
                    governmentId.isNotBlank() && birthDate.isNotBlank() &&
                    number.isNotBlank() && street.isNotBlank() &&
                    postalCode.isNotBlank() && phoneNumber.isNotBlank() &&
                    email.isNotBlank() && password.isNotBlank() &&
                    confirmPassword.isNotBlank() && city.isNotBlank() &&
                    province.isNotBlank() && isChecked &&
                    nameError == null && surnameError == null &&
                    governmentIdError == null && birthDateError == null &&
                    numberError == null && streetError == null &&
                    postalCodeError == null && phoneNumberError == null &&
                    emailError == null && passwordError == null &&
                    confirmPasswordError == null && cityError == null &&
                    provinceError == null
        }
    }

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            onRegistrationSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 30.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = stringResource(R.string.logo), modifier = Modifier
            .size(90.dp)
            .padding(top = 40.dp))
        Spacer(modifier = Modifier.height(60.dp))

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(stringResource(R.string.take_care_of_your_health), fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(250.dp))
            Text(stringResource(R.string.create_an_account), fontSize = 16.sp, fontWeight = FontWeight.W500, color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(30.dp))

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AuthTextField(
                value = name,
                onValueChange = { viewModel.onNameChanged(it) },
                fieldText = stringResource(R.string.name),
                hintText = stringResource(R.string.enter_your_first_name),
                errorMessage = nameError ?: "",
                onFocusLost = { viewModel.validateName() }
            )

            AuthTextField(
                value = surname,
                onValueChange = { viewModel.onSurnameChanged(it) },
                fieldText = stringResource(R.string.surname),
                hintText = stringResource(R.string.enter_your_last_name),
                errorMessage = surnameError ?: "",
                onFocusLost = { viewModel.validateSurname() }
            )

            AuthTextField(
                value = governmentId,
                onValueChange = { viewModel.onGovernmentIdChanged(it) },
                fieldText = stringResource(R.string.government_id),
                hintText = stringResource(R.string.enter_your_government_id),
                errorMessage = governmentIdError ?: "",
                onFocusLost = { viewModel.validateGovernmentId() }
            )

            Column {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { },
                    placeholder = { Text(stringResource(R.string.birth_date), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    isError = birthDateError != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val cal = Calendar.getInstance()
                            if (birthDate.isNotEmpty()) {
                                val parts = birthDate.split("-")
                                if (parts.size == 3) {
                                    cal.set(
                                        parts[2].toInt(),
                                        parts[1].toInt() - 1,
                                        parts[0].toInt()
                                    )
                                }
                            }

                            val dpd = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val formatted =
                                        String.format("%02d-%02d-%04d", dayOfMonth, month + 1, year)
                                    viewModel.onBirthDateChanged(formatted)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            )
                            dpd.show()
                        },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = if (birthDateError != null) MaterialTheme.colorScheme.error else Color.Transparent,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        disabledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                if (birthDateError != null) {
                    Text(
                        text = birthDateError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            SearchableProvinceDropdown(
                viewModel = viewModel,
                selectedProvince = province,
                onProvinceSelected = { viewModel.onProvinceChanged(it) },
                errorMessage = provinceError ?: "",
                onFocusLost = { viewModel.validateProvince() }
            )

            AuthTextField(
                value = postalCode,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() || it == '-' }.take(6)
                    
                    val formatted = if (filtered.length == 2 && !filtered.contains('-')) {
                        "$filtered-"
                    } else {
                        filtered
                    }
                    
                    viewModel.onPostalCodeChanged(formatted)
                },
                fieldText = stringResource(R.string.postal_code),
                hintText = stringResource(R.string.xx_xxx_format),
                keyboardType = KeyboardType.Number,
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
                    fieldText = stringResource(R.string.number),
                    hintText = stringResource(R.string.enter_number),
                    modifier = Modifier.width(130.dp),
                    errorMessage = numberError ?: "",
                    onFocusLost = { viewModel.validateNumber() }
                )

                AuthTextField(
                    value = street,
                    onValueChange = { viewModel.onStreetChanged(it) },
                    fieldText = stringResource(R.string.street),
                    hintText = stringResource(R.string.enter_street),
                    errorMessage = streetError ?: "",
                    onFocusLost = { viewModel.validateStreet() }
                )
            }

            AuthTextField(
                value = phoneNumber,
                onValueChange = { viewModel.onPhoneNumberChanged(it) },
                fieldText = stringResource(R.string.phone_number),
                hintText = stringResource(R.string.enter_your_phone_number),
                keyboardType = KeyboardType.Phone,
                errorMessage = phoneNumberError ?: "",
                modifier = Modifier.testTag("phone_number"),
                onFocusLost = { viewModel.validatePhoneNumber() }
            )

            AuthTextField(
                value = email,
                onValueChange = { viewModel.onEmailChanged(it) },
                fieldText = stringResource(R.string.email_address),
                hintText = stringResource(R.string.enter_your_email_address),
                keyboardType = KeyboardType.Email,
                errorMessage = emailError ?: "",
                modifier = Modifier.testTag("email_field"),
                onFocusLost = { viewModel.validateEmail() }
            )

            AuthTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChanged(it) },
                fieldText = stringResource(R.string.password),
                hintText = stringResource(R.string.enter_your_password),
                isPassword = true,
                errorMessage = passwordError ?: "",
                modifier = Modifier.testTag("password_field"),
                onFocusLost = { viewModel.validatePassword() }
            )

            AuthTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChanged(it) },
                fieldText = stringResource(R.string.confirm_password),
                hintText = stringResource(R.string.re_enter_your_password),
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
            Text(stringResource(R.string.accept_terms_conditions), fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
        ) {
            Text(
                text = stringResource(R.string.sign_up_capitals),
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Text(stringResource(R.string.already_have_an_account), fontWeight = FontWeight.W400, fontSize = 14.sp)
            Text(stringResource(R.string.sign_in_capital), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable {
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
