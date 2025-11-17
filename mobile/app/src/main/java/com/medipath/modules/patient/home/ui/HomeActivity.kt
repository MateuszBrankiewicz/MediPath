package com.medipath.modules.patient.home.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.medipath.core.theme.MediPathTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MedicalInformation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.network.RetrofitInstance
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.InfoCard
import com.medipath.modules.shared.components.MenuCard
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.components.NavigationRouter
import com.medipath.modules.shared.components.SearchBar
import com.medipath.modules.shared.components.VisitItem
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.patient.booking.ui.RescheduleVisitActivity
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.patient.visits.VisitsViewModel
import com.medipath.modules.patient.visits.ui.VisitDetailsActivity
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.settings.ui.SettingsActivity
import com.medipath.MediPathApplication
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val sessionManager = RetrofitInstance.getSessionManager()
        val authService = RetrofitInstance.authService

        checkNotificationPermission()

        setContent {
            MediPathTheme {
                HomeScreen(
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                authService.logout()
                            } catch (e: Exception) {
                                Log.e("HomeActivity", "Logout API error", e)
                            }

                            sessionManager.deleteSessionId()

                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(this@HomeActivity, LoginActivity::class.java)
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
fun HomeScreen(
    onLogoutClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
    ) {
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()

    val notificationsViewModel: NotificationsViewModel = viewModel()

    val visitsViewModel: VisitsViewModel = viewModel()
    val visitsError by visitsViewModel.error.collectAsState(null)

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchUserProfile()
                notificationsViewModel.fetchNotifications()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(visitsError) {
        if (visitsError != null) {
            Toast.makeText(context, visitsError, Toast.LENGTH_SHORT).show()
        }
    }

    val cancelSuccess by visitsViewModel.cancelSuccess.collectAsState()
    LaunchedEffect(cancelSuccess) {
        if (cancelSuccess) {
            viewModel.fetchUserProfile()
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val firstName by viewModel.firstName.collectAsState()
        val lastName by viewModel.lastName.collectAsState()
        val upcomingVisits by viewModel.upcomingVisits.collectAsState()
        val prescriptionCode by viewModel.prescriptionCode.collectAsState()
        val referralCode by viewModel.referralCode.collectAsState()
        val canBeDoctor by viewModel.canBeDoctor.collectAsState()
        var currentTab by remember { mutableStateOf("Dashboard") }

        Navigation(
            notificationsViewModel = notificationsViewModel,
            onNotificationsClick = {
                context.startActivity(Intent(context, NotificationsActivity::class.java))
            },
            onEditProfileClick = {
                context.startActivity(Intent(context, EditProfileActivity::class.java))
            },
            onSettingsClick = {
                context.startActivity(Intent(context, SettingsActivity::class.java))
            },
            firstName = firstName,
            lastName = lastName,
            currentTab = currentTab,
            canSwitchRole = canBeDoctor,
            content = { innerPadding ->
                val colors = LocalCustomColors.current

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    item {
                        SearchBar()
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .padding(vertical = 20.dp, horizontal = 30.dp)
                        ) {
                            Text(
                                text = "Dashboard",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            InfoCard(
                                title = "Prescriptions",
                                label = "Code:",
                                code = prescriptionCode.ifEmpty { "No active prescriptions" },
                                onClick = {
                                    NavigationRouter.navigateToTab(context, "Prescriptions", "Dashboard")
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            InfoCard(
                                title = "Referrals",
                                label = "Code:",
                                code = referralCode.ifEmpty { "No active referrals" },
                                onClick = {
                                    NavigationRouter.navigateToTab(context, "Referrals", "Dashboard")
                                }
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MenuCard(
                                    icon = Icons.AutoMirrored.Outlined.List,
                                    title = "Visits",
                                    onClick = { 
                                        NavigationRouter.navigateToTab(context, "Visits", "Dashboard")
                                    },
                                    backgroundColor = colors.purple800,
                                    iconColor = colors.purple300
                                )

                                MenuCard(
                                    icon = Icons.Outlined.MedicalInformation,
                                    title = "Medical history",
                                    onClick = { 
                                        NavigationRouter.navigateToTab(context, "Medical history", "Dashboard")
                                    },
                                    backgroundColor = colors.blue800,
                                    iconColor = colors.blue300
                                )

                                MenuCard(
                                    icon = Icons.AutoMirrored.Outlined.Comment,
                                    title = "Comments",
                                    onClick = { 
                                        NavigationRouter.navigateToTab(context, "Comments", "Dashboard")
                                    },
                                    backgroundColor = colors.orange800,
                                    iconColor = colors.orange300
                                )

                                MenuCard(
                                    icon = Icons.Outlined.Notifications,
                                    title = "Reminders",
                                    onClick = { 
                                        NavigationRouter.navigateToTab(context, "Reminders", "Dashboard")
                                    },
                                    backgroundColor = colors.green800,
                                    iconColor = colors.green300
                                )
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.background,
                                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 5.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    "Upcoming visits",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Visit",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            HorizontalDivider(thickness = 2.dp)
                        }
                    }

                    if (upcomingVisits.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(horizontal = 20.dp)
                            ) {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    "No upcoming visits",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    } else {
                        items(upcomingVisits) { visit ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(horizontal = 20.dp)
                            ) {
                                VisitItem(
                                    visit = visit,
                                    onCancelVisit = { visitId ->
                                        visitsViewModel.cancelVisit(visitId)
                                    },
                                    onViewDetails = { visitId ->
                                        val intent = Intent(context, VisitDetailsActivity::class.java)
                                        intent.putExtra("VISIT_ID", visitId)
                                        context.startActivity(intent)
                                    },
                                    onReschedule = { visitId ->
                                        val intent = Intent(context, RescheduleVisitActivity::class.java)
                                        intent.putExtra("visit_id", visit.id)
                                        intent.putExtra("doctor_id", visit.doctor.userId)
                                        intent.putExtra("doctor_name", "${visit.doctor.doctorName} ${visit.doctor.doctorSurname}")
                                        intent.putExtra("current_date", "${visit.time.startTime} at ${visit.institution.institutionName}")
                                        context.startActivity(intent)
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            },
            onLogoutClick = onLogoutClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MediPathTheme { HomeScreen() }
}