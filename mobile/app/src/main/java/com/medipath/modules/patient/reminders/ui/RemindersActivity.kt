package com.medipath.modules.patient.reminders.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.patient.notifications.ui.NotificationsActivity
import com.medipath.modules.patient.reminders.RemindersViewModel
import com.medipath.modules.patient.reminders.ui.components.ActionButtonsRow
import com.medipath.modules.patient.reminders.ui.components.FiltersSection
import com.medipath.modules.patient.reminders.ui.components.ReminderCard
import com.medipath.modules.patient.reminders.ui.components.StatisticsCards
import com.medipath.modules.patient.reminders.ui.components.TabSelector
import com.medipath.modules.patient.reminders.ui.components.SearchBar
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.settings.ui.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemindersActivity : ComponentActivity() {
    private lateinit var sessionManager: DataStoreSessionManager
    private var shouldRefresh = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sessionManager = DataStoreSessionManager(this)

        setContent {
            MediPathTheme {
                val refreshTrigger = remember { mutableStateOf(0) }

                LaunchedEffect(shouldRefresh) {
                    if (shouldRefresh) {
                        refreshTrigger.value++
                        shouldRefresh = false
                    }
                }

                RemindersScreen(
                    sessionManager = sessionManager,
                    refreshTrigger = refreshTrigger.value,
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                sessionManager.deleteSessionId()
                                withContext(Dispatchers.Main) {
                                    startActivity(Intent(this@RemindersActivity, LoginActivity::class.java))
                                    finish()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(this@RemindersActivity, "Logout error", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        shouldRefresh = true
    }
}

@Composable
fun RemindersScreen(
    sessionManager: DataStoreSessionManager,
    refreshTrigger: Int = 0,
    profileViewModel: HomeViewModel = remember { HomeViewModel() },
    remindersViewModel: RemindersViewModel = remember { RemindersViewModel() },
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val firstName by profileViewModel.firstName
    val lastName by profileViewModel.lastName
    val isProfileLoading by profileViewModel.isLoading

    val reminders by remindersViewModel.filteredReminders
    val isLoading by remindersViewModel.isLoading
    val totalReminders by remindersViewModel.totalReminders
    val unreadReminders by remindersViewModel.unreadReminders
    val todayReminders by remindersViewModel.todayReminders

    val statusFilter by remindersViewModel.statusFilter
    val dateFromFilter by remindersViewModel.dateFromFilter
    val dateToFilter by remindersViewModel.dateToFilter
    val sortBy by remindersViewModel.sortBy
    val searchQuery by remindersViewModel.searchQuery

    var showFilters by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Received") }

    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile(sessionManager)
        remindersViewModel.fetchReminders(sessionManager)
    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            remindersViewModel.fetchReminders(sessionManager)
        }
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
            screenTitle = "Reminders",
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
                        .padding(innerPadding)
                ) {
                    StatisticsCards(
                        totalReminders = totalReminders,
                        unreadReminders = unreadReminders,
                        todayReminders = todayReminders
                    )

                    TabSelector(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    ActionButtonsRow(
                        onShowFilters = { showFilters = !showFilters },
                        onClearFilters = { remindersViewModel.clearFilters() },
                        onAddReminder = {
                            context.startActivity(Intent(context, AddReminderActivity::class.java))
                        },
                        onRefresh = { remindersViewModel.fetchReminders(sessionManager) },
                        onMarkAllAsRead = { remindersViewModel.markAllAsRead(sessionManager) }
                    )

                    SearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { remindersViewModel.updateSearchQuery(it) }
                    )

                    if (showFilters) {
                        FiltersSection(
                            statusFilter = statusFilter,
                            dateFromFilter = dateFromFilter,
                            dateToFilter = dateToFilter,
                            sortBy = sortBy,
                            onStatusFilterChange = { remindersViewModel.updateStatusFilter(it) },
                            onDateFromChange = { remindersViewModel.updateDateFromFilter(it) },
                            onDateToChange = { remindersViewModel.updateDateToFilter(it) },
                            onSortByChange = { remindersViewModel.updateSortBy(it) }
                        )
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (reminders.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No reminders",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(reminders) { reminder ->
                                ReminderCard(
                                    reminder = reminder,
                                    onDelete = { remindersViewModel.deleteReminder(reminder) },
                                    onMarkAsRead = {
                                        remindersViewModel.markAsRead(
                                            reminder,
                                            sessionManager
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            },
            onLogoutClick = onLogoutClick,
            firstName = firstName,
            lastName = lastName,
            currentTab = "Reminders"
        )
    }
}