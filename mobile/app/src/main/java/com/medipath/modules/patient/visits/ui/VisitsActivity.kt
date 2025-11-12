package com.medipath.modules.patient.visits.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.modules.patient.visits.VisitsViewModel
import com.medipath.modules.patient.booking.ui.RescheduleVisitActivity
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.ActionButton
import com.medipath.modules.shared.components.FilterConfig
import com.medipath.modules.shared.components.GenericActionButtonsRow
import com.medipath.modules.shared.components.GenericFiltersSection
import com.medipath.modules.shared.components.GenericSearchBar
import com.medipath.modules.shared.components.GenericStatisticsCards
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.components.StatisticItem
import com.medipath.modules.shared.components.VisitItem
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.settings.ui.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VisitsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MediPathTheme {
                VisitsScreen(
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val authService = RetrofitInstance.authService
                            val sessionManager = RetrofitInstance.getSessionManager()
                            try {
                                authService.logout()
                            } catch (e: Exception) {
                                Log.e("VisitsActivity", "Logout API error", e)
                            }
                            sessionManager.deleteSessionId()
                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(this@VisitsActivity, LoginActivity::class.java)
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
fun VisitsScreen(
    profileViewModel: HomeViewModel = viewModel(),
    visitsViewModel: VisitsViewModel = viewModel(),
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val firstName by profileViewModel.firstName.collectAsState()
    val lastName by profileViewModel.lastName.collectAsState()
    val isProfileLoading by profileViewModel.isLoading.collectAsState()
    val profileShouldRedirect by profileViewModel.shouldRedirectToLogin.collectAsState()

    val colors = LocalCustomColors.current

    val visits by visitsViewModel.filteredVisits.collectAsState()
    val isLoading by visitsViewModel.isLoading.collectAsState()
    val visitsShouldRedirect by visitsViewModel.shouldRedirectToLogin.collectAsState()
    val totalVisits by visitsViewModel.totalVisits.collectAsState()
    val scheduledVisits by visitsViewModel.scheduledVisits.collectAsState()
    val completedVisits by visitsViewModel.completedVisits.collectAsState()

    val statusFilter by visitsViewModel.statusFilter.collectAsState()
    val dateFromFilter by visitsViewModel.dateFromFilter.collectAsState()
    val dateToFilter by visitsViewModel.dateToFilter.collectAsState()
    val sortBy by visitsViewModel.sortBy.collectAsState()
    val sortOrder by visitsViewModel.sortOrder.collectAsState()
    val searchQuery by visitsViewModel.searchQuery.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    val statisticsItems = remember(totalVisits, scheduledVisits, completedVisits) {
        listOf(
            StatisticItem(
                icon = Icons.Default.Event,
                iconTint = colors.blue800,
                label = "Total\nvisits",
                value = totalVisits.toString(),
                valueTint = colors.blue800
            ),
            StatisticItem(
                icon = Icons.Default.Schedule,
                iconTint = colors.orange800,
                label = "Scheduled\nvisits",
                value = scheduledVisits.toString(),
                valueTint = colors.orange800
            ),
            StatisticItem(
                icon = Icons.Default.CheckCircle,
                iconTint = colors.green800,
                label = "Completed\nvisits",
                value = completedVisits.toString(),
                valueTint = colors.green800
            )
        )
    }

    val actionButtons = remember {
        listOf(
            ActionButton(
                icon = Icons.Default.FilterList,
                label = "FILTERS",
                onClick = { showFilters = !showFilters },
                color = colors.blue800,
                isOutlined = true
            ),
            ActionButton(
                icon = Icons.Default.Clear,
                label = "CLEAR",
                onClick = { visitsViewModel.clearFilters() },
                color = colors.error,
                isOutlined = true
            ),
            ActionButton(
                icon = Icons.Default.Refresh,
                label = "REFRESH",
                onClick = { visitsViewModel.fetchVisits(upcoming = false) },
                color = colors.blue800,
                isOutlined = true
            )
        )
    }

    val visitsFilterConfig = remember {
        FilterConfig(
            statusOptions = listOf("All", "Scheduled", "Completed", "Cancelled"),
            sortByOptions = listOf("Date", "Doctor", "Institution"),
            sortOrderOptions = listOf("Ascending", "Descending"),
            showSortOrder = true
        )
    }

    val notificationsViewModel: NotificationsViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                profileViewModel.fetchUserProfile()
                visitsViewModel.fetchVisits(upcoming = false)
                notificationsViewModel.fetchNotifications()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val shouldRedirect = profileShouldRedirect || visitsShouldRedirect
    if (shouldRedirect) {
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
    } else if (isProfileLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Navigation(
            notificationsViewModel = notificationsViewModel,
            screenTitle = "Visits",
            onNotificationsClick = {
                context.startActivity(Intent(context, NotificationsActivity::class.java))
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
                        .padding(innerPadding),
                ) {
                    GenericStatisticsCards(statistics = statisticsItems)
                    
                    GenericActionButtonsRow(buttons = actionButtons)
                    
                    GenericSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { visitsViewModel.updateSearchQuery(it) },
                        placeholder = "Search by doctor, institution..."
                    )
                    
                    if (showFilters) {
                        GenericFiltersSection(
                            statusFilter = statusFilter,
                            dateFromFilter = dateFromFilter,
                            dateToFilter = dateToFilter,
                            sortBy = sortBy,
                            sortOrder = sortOrder,
                            onStatusFilterChange = { visitsViewModel.updateStatusFilter(it) },
                            onDateFromChange = { visitsViewModel.updateDateFromFilter(it) },
                            onDateToChange = { visitsViewModel.updateDateToFilter(it) },
                            onSortByChange = { visitsViewModel.updateSortBy(it) },
                            onSortOrderChange = { visitsViewModel.updateSortOrder(it) },
                            filterConfig = visitsFilterConfig
                        )
                    }
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (visits.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No visits",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 15.dp)
                        ) {
                            items(visits) { visit ->
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
                                        },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            },
            onLogoutClick = onLogoutClick,
            firstName = firstName,
            lastName = lastName,
            currentTab = "Visits"
        )
    }
}