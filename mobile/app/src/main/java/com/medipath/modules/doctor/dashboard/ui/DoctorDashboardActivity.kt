package com.medipath.modules.doctor.dashboard.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.settings.ui.SettingsActivity
import com.medipath.MediPathApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoctorDashboardActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()
        
        setContent {
            MediPathTheme {
                DoctorDashboardScreen(
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val authService = RetrofitInstance.authService
                            val sessionManager = RetrofitInstance.getSessionManager()
                            try {
                                authService.logout()
                            } catch (e: Exception) {
                                Log.e("DoctorDaschboardActivity", "Logout API error", e)
                            }
                            sessionManager.deleteSessionId()
                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(this@DoctorDashboardActivity, LoginActivity::class.java)
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

    private fun checkNotificationPermission() {
        val app = application as MediPathApplication
        if (app.shouldRequestNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            app.markPermissionRequested()
        }
    }
}

@Composable
fun DoctorDashboardScreen(
    viewModel: HomeViewModel = viewModel(),
    notificationsViewModel: NotificationsViewModel = viewModel(),
    onLogoutClick: () -> Unit = {}
) {
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        notificationsViewModel.fetchNotifications()
    }

    Navigation(
        notificationsViewModel = notificationsViewModel,
        screenTitle = null,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

            }
        },
        onLogoutClick = onLogoutClick,
        firstName = firstName,
        lastName = lastName,
        currentTab = "Dashboard",
        isDoctor = true,
        canSwitchRole = true
    )
}
