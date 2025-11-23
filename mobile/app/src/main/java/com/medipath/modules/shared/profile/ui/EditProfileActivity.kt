package com.medipath.modules.shared.profile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.R
import com.medipath.modules.shared.profile.ProfileViewModel
import com.medipath.modules.shared.profile.ui.components.*
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.MediPathTheme
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.shared.components.rememberBase64Image
import com.medipath.modules.shared.components.rememberBase64ImagePicker
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.shared.auth.RegisterViewModel
import com.medipath.modules.shared.auth.ui.LoginActivity

class EditProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MediPathTheme {
                EditProfileScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoadingAuth by viewModel.isLoading.collectAsState()
    val shouldRedirectToLogin by viewModel.shouldRedirectToLogin.collectAsState()

    val name by profileViewModel.name.collectAsState()
    val surname by profileViewModel.surname.collectAsState()
    val birthDate by profileViewModel.birthDate.collectAsState()
    val phoneNumber by profileViewModel.phoneNumber.collectAsState()
    val govId by profileViewModel.govId.collectAsState()
    val city by profileViewModel.city.collectAsState()
    val province by profileViewModel.province.collectAsState()
    val street by profileViewModel.street.collectAsState()
    val number by profileViewModel.number.collectAsState()
    val postalCode by profileViewModel.postalCode.collectAsState()
    val pfpImage by profileViewModel.pfpImage.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val updateSuccess by profileViewModel.updateSuccess.collectAsState()
    val resetSuccess by profileViewModel.resetSuccess.collectAsState()

    val nameError by profileViewModel.nameError.collectAsState()
    val surnameError by profileViewModel.surnameError.collectAsState()
    val phoneError by profileViewModel.phoneError.collectAsState()
    val cityError by profileViewModel.cityError.collectAsState()
    val provinceError by profileViewModel.provinceError.collectAsState()
    val streetError by profileViewModel.streetError.collectAsState()
    val numberError by profileViewModel.numberError.collectAsState()
    val postalCodeError by profileViewModel.postalCodeError.collectAsState()
    val currentPasswordError by profileViewModel.currentPasswordError.collectAsState()
    val newPasswordError by profileViewModel.newPasswordError.collectAsState()
    val registerViewModel: RegisterViewModel = viewModel()


    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        profileViewModel.fetchProfile()
    }

    LaunchedEffect(error) {
        if (error != null && error != "401") {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, context.getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(resetSuccess) {
        if (resetSuccess) {
            Toast.makeText(context, context.getString(R.string.password_changed), Toast.LENGTH_SHORT).show()
        }
    }

    if (shouldRedirectToLogin) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, context.getString(R.string.error_session), Toast.LENGTH_LONG)
                .show()
            val sessionManager = RetrofitInstance.getSessionManager()
            sessionManager.deleteSessionId()
            context.startActivity(
                Intent(context, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            (context as? ComponentActivity)?.finish()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.edit_profile),
                        color = MaterialTheme.colorScheme.background,
                        fontSize = 23.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.background
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LocalCustomColors.current.blue900
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.secondary)
        ) {
            when {
                !shouldRedirectToLogin -> {
                    when {
                        isLoading || isLoadingAuth -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        else -> {
                            var editedName by remember { mutableStateOf(name) }
                            var editedSurname by remember { mutableStateOf(surname) }
                            var editedPhone by remember { mutableStateOf(phoneNumber) }
                            var editedCity by remember { mutableStateOf(city) }
                            var editedProvince by remember { mutableStateOf(province) }
                            var editedStreet by remember { mutableStateOf(street) }
                            var editedNumber by remember { mutableStateOf(number) }
                            var editedPostal by remember { mutableStateOf(postalCode) }

                            LaunchedEffect(
                                name,
                                surname,
                                phoneNumber,
                                city,
                                province,
                                street,
                                number,
                                postalCode
                            ) {
                                editedName = name
                                editedSurname = surname
                                editedPhone = phoneNumber
                                editedCity = city
                                editedProvince = province
                                editedStreet = street
                                editedNumber = number
                                editedPostal = postalCode
                            }

                            var currentPassword by remember { mutableStateOf("") }
                            var newPassword by remember { mutableStateOf("") }

                            val imagePickerLauncher = rememberBase64ImagePicker { base64Image ->
                                profileViewModel.setPfpImage(base64Image)
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                verticalArrangement = Arrangement.Top
                            ) {
                                item { Spacer(modifier = Modifier.height(20.dp)) }

                                item {
                                    val profileImage = rememberBase64Image(pfpImage)
                                    ProfilePictureSection(
                                        profileImage = profileImage,
                                        imagePickerLauncher = imagePickerLauncher
                                    )
                                }

                                item {
                                    PersonalInformationSection(
                                        editedName = editedName,
                                        onNameChange = {
                                            editedName = it; profileViewModel.setName(
                                            it
                                        )
                                        },
                                        nameError = nameError,
                                        editedSurname = editedSurname,
                                        onSurnameChange = {
                                            editedSurname = it; profileViewModel.setSurname(it)
                                        },
                                        surnameError = surnameError,
                                        birthDate = birthDate,
                                        govId = govId
                                    )
                                }

                                item {
                                    ContactAddressSection(
                                        viewModel = registerViewModel,
                                        editedPhone = editedPhone,
                                        onPhoneChange = {
                                            editedPhone = it; profileViewModel.setPhoneNumber(it)
                                        },
                                        phoneError = phoneError,
                                        editedCity = editedCity,
                                        onCityChange = {
                                            editedCity = it; profileViewModel.setCity(
                                            it
                                        )
                                        },
                                        cityError = cityError,
                                        editedProvince = editedProvince,
                                        onProvinceChange = {
                                            editedProvince = it; profileViewModel.setProvince(it)
                                        },
                                        provinceError = provinceError,
                                        editedPostal = editedPostal,
                                        onPostalChange = {
                                            editedPostal = it; profileViewModel.setPostalCode(it)
                                        },
                                        postalCodeError = postalCodeError,
                                        editedNumber = editedNumber,
                                        onNumberChange = {
                                            editedNumber = it; profileViewModel.setNumber(it)
                                        },
                                        numberError = numberError,
                                        editedStreet = editedStreet,
                                        onStreetChange = {
                                            editedStreet = it; profileViewModel.setStreet(it)
                                        },
                                        streetError = streetError
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { profileViewModel.updateProfile() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        shape = RoundedCornerShape(30.dp),
                                        enabled = true,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.save_changes),
                                            color = MaterialTheme.colorScheme.background
                                        )
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    HorizontalDivider()
                                }

                                item {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    ChangePasswordSection(
                                        currentPassword = currentPassword,
                                        onCurrentPasswordChange = {
                                            currentPassword = it
                                            profileViewModel.validateCurrentPassword(it)
                                        },
                                        currentPasswordError = currentPasswordError,
                                        newPassword = newPassword,
                                        onNewPasswordChange = {
                                            newPassword = it
                                            profileViewModel.validateNewPassword(it)
                                        },
                                        newPasswordError = newPasswordError,
                                        onResetPassword = {
                                            profileViewModel.resetPassword(
                                                currentPassword,
                                                newPassword
                                            )
                                        },
                                        enabled = currentPassword.isNotBlank() && newPassword.isNotBlank()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
