package com.medipath.modules.doctor.patients.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.doctor.patients.DoctorPatientsViewModel
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.settings.ui.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoctorPatientsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MediPathTheme {
                DoctorPatientsScreen(
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val authService = RetrofitInstance.authService
                            val sessionManager = RetrofitInstance.getSessionManager()
                            try {
                                authService.logout()
                            } catch (e: Exception) {
                                Log.e("DoctorPatientActivity", "Logout API error", e)
                            }
                            sessionManager.deleteSessionId()
                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(this@DoctorPatientsActivity, LoginActivity::class.java)
                                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                )
                                finish()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DoctorPatientsScreen(
    viewModel: HomeViewModel = viewModel(),
    patientsViewModel: DoctorPatientsViewModel = viewModel(),
    notificationsViewModel: NotificationsViewModel = viewModel(),
    onLogoutClick: () -> Unit = {}
) {
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val patients by patientsViewModel.patients.collectAsState()
    val isLoading by patientsViewModel.isLoading.collectAsState()
    val errorMessage by patientsViewModel.errorMessage.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        notificationsViewModel.fetchNotifications()
        patientsViewModel.fetchPatients()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Navigation(
        notificationsViewModel = notificationsViewModel,
        screenTitle = "Recent Patients",
        onNotificationsClick = {
            val intent = Intent(context, NotificationsActivity::class.java)
            intent.putExtra("isDoctor", true)
            context.startActivity(intent)
        },
        onEditProfileClick = {
            context.startActivity(Intent(context, EditProfileActivity::class.java))
        },
        onSettingsClick = {
            context.startActivity(Intent(context, SettingsActivity::class.java))
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(innerPadding)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    patients.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No patients found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(patients) { patient ->
                                PatientCard(
                                    patient = patient,
                                    onClick = {}
                                )
                            }
                        }
                    }
                }
            }
        },
        firstName = firstName,
        lastName = lastName,
        currentTab = "Patients",
        isDoctor = true,
        canSwitchRole = true,
        onLogoutClick = onLogoutClick
    )
}
