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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.shared.profile.ProfileViewModel
import com.medipath.modules.doctor.dashboard.DoctorDashboardViewModel
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.settings.ui.SettingsActivity
import com.medipath.modules.doctor.visit.ui.DoctorVisitDetailsActivity
import com.medipath.MediPathApplication
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.utils.LocaleHelper
import com.medipath.modules.doctor.dashboard.ui.components.CalendarGrid
import com.google.gson.Gson
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.medipath.R
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoctorDashboardActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this,
                getString(R.string.notification_permission_granted), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this,
                getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }
    
    private var shouldRefreshOnResume by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = RetrofitInstance.getSessionManager()
        if (sessionManager.isLoggedIn()) {
            (application as MediPathApplication).initializeWebSocket()
        }
        
        checkNotificationPermission()
        
        setContent {
            MediPathTheme {
                DoctorDashboardScreen(
                    shouldRefresh = shouldRefreshOnResume,
                    onRefreshHandled = { shouldRefreshOnResume = false },
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

    override fun onResume() {
        super.onResume()
        shouldRefreshOnResume = true
        
        val sessionManager = RetrofitInstance.getSessionManager()
        if (sessionManager.isLoggedIn()) {
            (application as MediPathApplication).reconnectWebSocketIfNeeded()
        }
    }

    private fun checkNotificationPermission() {
        val app = application as MediPathApplication
        if (app.shouldRequestNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun DoctorDashboardScreen(
    shouldRefresh: Boolean = false,
    onRefreshHandled: () -> Unit = {},
    dashboardViewModel: DoctorDashboardViewModel = viewModel(),
    notificationsViewModel: NotificationsViewModel = viewModel(),
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel()

    val name by viewModel.name.collectAsState()
    val surname by viewModel.surname.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val numOfRatings by viewModel.numOfRatings.collectAsState()
    val canSwitchRole by viewModel.canSwitchRole.collectAsState()
    
    val selectedDateVisits by dashboardViewModel.selectedDateVisits.collectAsState()
    val currentVisit by dashboardViewModel.currentVisit.collectAsState()
    val patientCount by dashboardViewModel.selectedDatePatientCount.collectAsState()
    
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val colors = LocalCustomColors.current

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
        notificationsViewModel.fetchNotifications()
        dashboardViewModel.fetchVisitsForDate(LocalDate.now())
    }
    
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            dashboardViewModel.fetchVisitsForDate(selectedDate)
            onRefreshHandled()
        }
    }
    
    LaunchedEffect(selectedDate) {
        dashboardViewModel.fetchVisitsForDate(selectedDate)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        text = String.format("%.1f", rating),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = "/5",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Text(
                                    text = if (numOfRatings > 0) 
                                        stringResource(R.string.satisfied_patients, numOfRatings)
                                    else 
                                        stringResource(R.string.no_ratings_yet),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.Star,
                                contentDescription = stringResource(R.string.rating),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.blue900
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "$patientCount",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = if (selectedDate.isEqual(LocalDate.now())) 
                                        stringResource(R.string.patients_for_today)
                                    else 
                                        stringResource(R.string.patients_for_date, selectedDate.format(DateTimeFormatter.ofPattern("dd.MM"))),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = stringResource(R.string.patients),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.select_date),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(onClick = { 
                                    selectedDate = LocalDate.now()
                                    currentMonth = YearMonth.now()
                                }) {
                                    Text(stringResource(R.string.today))
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { 
                                    currentMonth = currentMonth.minusMonths(1)
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                                        contentDescription = stringResource(R.string.previous_month)
                                    )
                                }
                                
                                val locale = LocaleHelper.getLocale(context)
                                Text(
                                    text = currentMonth.format(
                                        DateTimeFormatter.ofPattern("LLLL yyyy", locale)
                                    ).replaceFirstChar { it.uppercase() },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                IconButton(onClick = { 
                                    currentMonth = currentMonth.plusMonths(1)
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                                        contentDescription = stringResource(R.string.next_month)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            CalendarGrid(
                                yearMonth = currentMonth,
                                selectedDate = selectedDate,
                                onDateSelected = { date ->
                                    selectedDate = date
                                }
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.current_visit),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            if (currentVisit != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val intent = Intent(
                                                context,
                                                DoctorVisitDetailsActivity::class.java
                                            )
                                            intent.putExtra(
                                                "VISIT_JSON",
                                                Gson().toJson(currentVisit)
                                            )
                                            intent.putExtra("IS_CURRENT", true)
                                            context.startActivity(intent)
                                        }
                                ) {
                                    Text(
                                        text = "${currentVisit!!.patient.name} ${currentVisit!!.patient.surname}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "Time: ${currentVisit!!.time.startTime.substring(11, 16)} - ${currentVisit!!.time.endTime.substring(11, 16)}",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    if (!currentVisit!!.patientRemarks.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Remarks: ${currentVisit!!.patientRemarks}",
                                            fontSize = 14.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.no_appointment_scheduled),
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = if (selectedDate.isEqual(LocalDate.now())) 
                                    stringResource(R.string.visits_for_today)
                                else 
                                    "${stringResource(R.string.visits)} (${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            if (selectedDateVisits.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_appointments_scheduled),
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            } else {
                                selectedDateVisits.forEachIndexed { index, visit ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val intent = Intent(
                                                    context,
                                                    DoctorVisitDetailsActivity::class.java
                                                )
                                                intent.putExtra("VISIT_JSON", Gson().toJson(visit))
                                                intent.putExtra("IS_CURRENT", false)
                                                context.startActivity(intent)
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "${visit.patient.name} ${visit.patient.surname}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            
                                            Spacer(modifier = Modifier.height(4.dp))
                                            
                                            Text(
                                                text = "${visit.time.startTime.substring(11, 16)} - ${visit.time.endTime.substring(11, 16)}",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                            
                                            if (!visit.patientRemarks.isNullOrEmpty()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = visit.patientRemarks,
                                                    fontSize = 13.sp,
                                                    fontStyle = FontStyle.Italic,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                        
                                        Surface(
                                            color = when(visit.status) {
                                                "Upcoming" -> colors.green800.copy(alpha = 0.2f)
                                                "Completed" -> colors.blue800.copy(alpha = 0.2f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = visit.status,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = when(visit.status) {
                                                    "Upcoming" -> colors.green800
                                                    "Completed" -> colors.blue800
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        onLogoutClick = onLogoutClick,
        firstName = name,
        lastName = surname,
        currentTab = stringResource(R.string.dashboard),
        isDoctor = true,
        canSwitchRole = canSwitchRole
    )
}