package com.medipath.modules.shared.settings.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.modules.shared.settings.SettingsViewModel
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.MediPathTheme
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.shared.auth.ui.LoginActivity

class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MediPathTheme {
                SettingsScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoadingAuth by viewModel.isLoading.collectAsState()
    val shouldRedirectToLogin by viewModel.shouldRedirectToLogin.collectAsState()

    val language by settingsViewModel.language.collectAsState()
    val systemNotifications by settingsViewModel.systemNotifications.collectAsState()
    val userNotifications by settingsViewModel.userNotifications.collectAsState()
    val isLoading by settingsViewModel.isLoading.collectAsState()
    val error by settingsViewModel.error.collectAsState()
    val updateSuccess by settingsViewModel.updateSuccess.collectAsState()

    val deactivateSuccess by settingsViewModel.deactivateSuccess.collectAsState()
    val deactivateError by settingsViewModel.error.collectAsState()
    val isDeactivating by settingsViewModel.isLoading.collectAsState()

    var showDeactivateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        settingsViewModel.fetchSettings()
    }

    if (shouldRedirectToLogin) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            val sessionManager = RetrofitInstance.getSessionManager()
            sessionManager.deleteSessionId()
            context.startActivity(
                Intent(context, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            (context as? ComponentActivity)?.finish()
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(deactivateError) {
        if (deactivateError != null) {
            Toast.makeText(context, deactivateError, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(deactivateSuccess) {
        if (deactivateSuccess) {
            Toast.makeText(context, "Account deactivated successfully", Toast.LENGTH_LONG).show()
            val sessionManager = RetrofitInstance.getSessionManager()
            sessionManager.deleteSessionId()
            context.startActivity(
                Intent(context, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            (context as? ComponentActivity)?.finish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalCustomColors.current.blue900)
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Return",
                    tint = MaterialTheme.colorScheme.background
                )
            }
            Text(
                text = "Settings",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(start = 8.dp).padding(vertical = 24.dp)
            )
        }

        if (!shouldRedirectToLogin) {
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        item { Spacer(modifier = Modifier.height(20.dp)) }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Language",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = language == "PL",
                                            onClick = { settingsViewModel.setLanguage("PL") }
                                        )
                                        Text(text = "Polski (PL)", modifier = Modifier.padding(start = 8.dp))
                                        Spacer(modifier = Modifier.width(16.dp))
                                        RadioButton(
                                            selected = language == "EN",
                                            onClick = { settingsViewModel.setLanguage("EN") }
                                        )
                                        Text(text = "English (EN)", modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Notifications",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "System notifications",
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Switch(
                                            checked = systemNotifications,
                                            onCheckedChange = { settingsViewModel.setSystemNotifications(it) },
                                            colors = SwitchDefaults.colors(
                                                uncheckedTrackColor = MaterialTheme.colorScheme.background
                                            )
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "User notifications",
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Switch(
                                            checked = userNotifications,
                                            onCheckedChange = { settingsViewModel.setUserNotifications(it) },
                                            colors = SwitchDefaults.colors(
                                                uncheckedTrackColor = MaterialTheme.colorScheme.background
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { settingsViewModel.updateSettings() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(30.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Text(text = "SAVE SETTINGS")
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Account",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { showDeactivateDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isDeactivating,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = LocalCustomColors.current.red800
                                        )
                                    ) {
                                        if (isDeactivating) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = MaterialTheme.colorScheme.background
                                            )
                                        } else {
                                            Text("DEACTIVATE ACCOUNT", color = MaterialTheme.colorScheme.background)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeactivateDialog) {
            AlertDialog(
                onDismissRequest = { showDeactivateDialog = false },
                title = { Text("Deactivate Account") },
                text = { 
                    Text("Are you sure you want to deactivate your account? This action cannot be undone and you will be logged out.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeactivateDialog = false
                            settingsViewModel.deactivateAccount()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LocalCustomColors.current.red800
                        )
                    ) {
                        Text("Deactivate")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeactivateDialog = false }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            )
        }
    }
}
