package com.medipath.modules.patient.visits.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.*
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
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.patient.notifications.ui.NotificationsActivity
import com.medipath.modules.patient.visits.VisitsViewModel
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
        val sessionManager = DataStoreSessionManager(this)

        setContent {
            MediPathTheme {
                VisitsScreen(
                    sessionManager = sessionManager,
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                sessionManager.deleteSessionId()
                                withContext(Dispatchers.Main) {
                                    startActivity(Intent(this@VisitsActivity, LoginActivity::class.java))
                                    finish()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@VisitsActivity, "Logout error", Toast.LENGTH_LONG).show()
                                }
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
    sessionManager: DataStoreSessionManager,
    profileViewModel: HomeViewModel = remember { HomeViewModel() },
    visitsViewModel: VisitsViewModel = remember { VisitsViewModel() },
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val firstName by profileViewModel.firstName
    val lastName by profileViewModel.lastName
    val isProfileLoading by profileViewModel.isLoading
    val colors = LocalCustomColors.current

    val visits by visitsViewModel.filteredVisits
    val isLoading by visitsViewModel.isLoading
    val totalVisits by visitsViewModel.totalVisits
    val scheduledVisits by visitsViewModel.scheduledVisits
    val completedVisits by visitsViewModel.completedVisits

    val statusFilter by visitsViewModel.statusFilter
    val dateFromFilter by visitsViewModel.dateFromFilter
    val dateToFilter by visitsViewModel.dateToFilter
    val sortBy by visitsViewModel.sortBy
    val sortOrder by visitsViewModel.sortOrder
    val searchQuery by visitsViewModel.searchQuery

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
                onClick = { visitsViewModel.fetchVisits(sessionManager, upcoming = false) },
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

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile(sessionManager)
        visitsViewModel.fetchVisits(sessionManager, upcoming = false)
    }

    if (isProfileLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Navigation(
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
            onSelectRoleClick = {
                Toast.makeText(context, "Select Role", Toast.LENGTH_SHORT).show()
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
                            verticalArrangement = Arrangement.Top
                        ) {
                            items(visits) { visit ->
                                Column(
                                    modifier = Modifier.padding(horizontal = 30.dp)
                                ) {
                                    VisitItem(
                                        visit = visit,
                                        onCancelVisit = { visitId ->
                                            visitsViewModel.cancelVisit(visitId, sessionManager)
                                        },
                                        onViewDetails = { visitId ->
                                            val intent = Intent(context, VisitDetailsActivity::class.java)
                                            intent.putExtra("VISIT_ID", visitId)
                                            context.startActivity(intent)
                                        },
                                        onReschedule = { visitId ->
                                            Toast.makeText(context, "Reschedule visit: $visitId", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
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