package com.medipath.modules.doctor.schedule.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.models.DoctorScheduleItem
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.doctor.schedule.DoctorScheduleViewModel
import com.medipath.modules.doctor.schedule.ui.components.ScheduleDetailsDialog
import com.medipath.modules.doctor.schedule.ui.components.ScheduleItemCard
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.patient.visits.VisitsViewModel
import com.medipath.modules.shared.components.CalendarCard
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.settings.ui.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DoctorScheduleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MediPathTheme {
                DoctorScheduleScreen(
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val authService = RetrofitInstance.authService
                            val sessionManager = RetrofitInstance.getSessionManager()
                            try {
                                authService.logout()
                            } catch (e: Exception) {
                                Log.e("DoctorScheduleActivity", "Logout API error", e)
                            }
                            sessionManager.deleteSessionId()
                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(this@DoctorScheduleActivity, LoginActivity::class.java)
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
fun DoctorScheduleScreen(
    viewModel: HomeViewModel = viewModel(),
    notificationsViewModel: NotificationsViewModel = viewModel(),
    scheduleViewModel: DoctorScheduleViewModel = viewModel(),
    visitsViewModel: VisitsViewModel = viewModel(),
    onLogoutClick: () -> Unit = {}
) {
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val context = LocalContext.current

    val schedules by scheduleViewModel.schedules.collectAsState()
    val isLoading by scheduleViewModel.isLoading.collectAsState()
    val error by scheduleViewModel.error.collectAsState()
    val cancelSuccess by visitsViewModel.cancelSuccess.collectAsState()
    val cancelError by visitsViewModel.error.collectAsState()

    var currentMonth by remember { mutableStateOf(LocalDate.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedSchedule by remember { mutableStateOf<DoctorScheduleItem?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
        notificationsViewModel.fetchNotifications()
    }

    LaunchedEffect(cancelSuccess) {
        if (cancelSuccess) {
            Toast.makeText(context, "Appointment cancelled successfully", Toast.LENGTH_SHORT).show()
            scheduleViewModel.fetchSchedules()
            showDetailsDialog = false
            showCancelDialog = false
        }
    }

    LaunchedEffect(cancelError) {
        cancelError?.let { errorMsg ->
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
        }
    }

    val schedulesByDate = remember(schedules) {
        schedules
            .groupBy {
                LocalDateTime.parse(it.startHour, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .toLocalDate()
            }
            .toSortedMap()
    }

    val dateCountMap = remember(schedulesByDate) {
        schedulesByDate.mapValues { it.value.size }
    }

    val availableDates = remember(schedulesByDate) {
        val dates = schedulesByDate.keys.toSet()
        Log.d("DoctorSchedule", "Available dates: ${dates.size} - $dates")
        dates
    }

    val schedulesForSelectedDate = remember(selectedDate, schedulesByDate) {
        selectedDate?.let { schedulesByDate[it] } ?: emptyList()
    }

    val filteredSchedules = remember(schedulesForSelectedDate, selectedTab) {
        when (selectedTab) {
            0 -> schedulesForSelectedDate
            1 -> schedulesForSelectedDate.filter { it.booked }
            2 -> schedulesForSelectedDate.filter { !it.booked }
            else -> schedulesForSelectedDate
        }
    }

    if (showDetailsDialog && selectedSchedule != null) {
        ScheduleDetailsDialog(
            schedule = selectedSchedule,
            onDismiss = { showDetailsDialog = false },
            onCancel = {
                showDetailsDialog = false
                showCancelDialog = true
            },
            onReschedule = {
                showDetailsDialog = false
            }
        )
    }

    if (showCancelDialog && selectedSchedule != null) {
        val startDateTime = LocalDateTime.parse(
            selectedSchedule!!.startHour,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
        
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { 
                Text(text = "Confirm Cancellation") 
            },
            text = { 
                Column {
                    Text("Are you sure you want to cancel this appointment?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Date: ${startDateTime.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "Time: ${startDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedSchedule!!.visitId?.let { visitId ->
                            visitsViewModel.cancelVisit(visitId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LocalCustomColors.current.red800
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    Navigation(
        notificationsViewModel = notificationsViewModel,
        screenTitle = "Schedule",
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
                    .padding(horizontal = 20.dp),
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            item {
                                Text(
                                    text = "Select a date",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            item {
                                CalendarCard(
                                    currentMonth = currentMonth,
                                    availableDates = availableDates,
                                    dateCountMap = dateCountMap,
                                    selectedDate = selectedDate,
                                    onMonthChange = { currentMonth = it },
                                    onDateSelected = { selectedDate = it }
                                )
                            }

                            if (selectedDate != null) {
                                item {
                                    Text(
                                        text = selectedDate!!.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                                    )
                                }

                                item {
                                    TabRow(
                                        selectedTabIndex = selectedTab,
                                        containerColor = MaterialTheme.colorScheme.background,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        Tab(
                                            selected = selectedTab == 0,
                                            onClick = { selectedTab = 0 },
                                            text = {
                                                Text(
                                                    text = "All",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        )
                                        Tab(
                                            selected = selectedTab == 1,
                                            onClick = { selectedTab = 1 },
                                            text = {
                                                Text(
                                                    text = "Scheduled",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        )
                                        Tab(
                                            selected = selectedTab == 2,
                                            onClick = { selectedTab = 2 },
                                            text = {
                                                Text(
                                                    text = "Available",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        )
                                    }
                                }

                                if (filteredSchedules.isNotEmpty()) {
                                    items(filteredSchedules) { schedule ->
                                        ScheduleItemCard(
                                            schedule = schedule,
                                            onClick = {
                                                selectedSchedule = schedule
                                                showDetailsDialog = true
                                            }
                                        )
                                    }
                                } else {
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.background
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(32.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.EventBusy,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "No appointments",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
        },
        firstName = firstName,
        lastName = lastName,
        currentTab = "Schedule",
        isDoctor = true,
        canSwitchRole = true,
        onLogoutClick = onLogoutClick
    )
}
