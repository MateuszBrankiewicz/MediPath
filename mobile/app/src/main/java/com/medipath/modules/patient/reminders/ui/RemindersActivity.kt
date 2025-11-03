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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.patient.notifications.ui.NotificationsActivity
import com.medipath.modules.patient.reminders.RemindersViewModel
import com.medipath.modules.patient.reminders.ui.components.ReminderCard
import com.medipath.modules.patient.reminders.ui.components.TabSelector
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.ActionButton
import com.medipath.modules.shared.components.FilterConfig
import com.medipath.modules.shared.components.GenericActionButtonsRow
import com.medipath.modules.shared.components.GenericFiltersSection
import com.medipath.modules.shared.components.GenericSearchBar
import com.medipath.modules.shared.components.GenericStatisticsCards
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.components.StatisticItem
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
    val colors = LocalCustomColors.current

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

    val statisticsItems = remember(totalReminders, unreadReminders, todayReminders) {
        listOf(
            StatisticItem(
                icon = Icons.Default.Notifications,
                iconTint = colors.blue800,
                label = "Total\nnotifications",
                value = totalReminders.toString(),
                valueTint = colors.blue800
            ),
            StatisticItem(
                icon = Icons.Outlined.MailOutline,
                iconTint = colors.orange800,
                label = "Unread\nnotifications",
                value = unreadReminders.toString(),
                valueTint = colors.orange800
            ),
            StatisticItem(
                icon = Icons.Default.Today,
                iconTint = colors.green800,
                label = "Today's\nnotifications",
                value = todayReminders.toString(),
                valueTint = colors.green800
            )
        )
    }

    val actionButtons = remember {
        listOf(
            ActionButton(
                icon = Icons.Default.Add,
                label = "ADD",
                onClick = { context.startActivity(Intent(context, AddReminderActivity::class.java)) },
                color = colors.green800,
                isOutlined = false
            ),
            ActionButton(
                icon = Icons.Default.Refresh,
                label = "REFRESH",
                onClick = { remindersViewModel.fetchReminders(sessionManager) },
                color = colors.blue800,
                isOutlined = true
            ),
            ActionButton(
                icon = Icons.Default.DoneAll,
                label = "MARK ALL",
                onClick = { remindersViewModel.markAllAsRead(sessionManager) },
                color = colors.blue900,
                isOutlined = false
            ),
            ActionButton(
                icon = Icons.Default.FilterList,
                label = "FILTERS",
                onClick = { showFilters = !showFilters },
                color = colors.blue800,
                isOutlined = true
            ),
            ActionButton(
                icon = Icons.Default.Clear,
                label = "CLEAR FILTERS",
                onClick = { remindersViewModel.clearFilters() },
                color = colors.error,
                isOutlined = true
            )
        )
    }

    val remindersFilterConfig = remember {
        FilterConfig(
            statusOptions = listOf("All", "Read", "Unread"),
            sortByOptions = listOf("Date", "Title"),
            sortOrderOptions = listOf("Ascending", "Descending"),
            showSortOrder = true
        )
    }

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
                    GenericStatisticsCards(
                        statistics = statisticsItems,
                        modifier = Modifier.padding(top = 0.dp)
                    )

                    TabSelector(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )

                    GenericActionButtonsRow(
                        buttons = actionButtons,
                        buttonsPerRow = 3
                    )

                    GenericSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { remindersViewModel.updateSearchQuery(it) },
                        placeholder = "Search"
                    )

                    if (showFilters) {
                        GenericFiltersSection(
                            statusFilter = statusFilter,
                            dateFromFilter = dateFromFilter,
                            dateToFilter = dateToFilter,
                            sortBy = sortBy,
                            sortOrder = "Ascending",
                            onStatusFilterChange = { remindersViewModel.updateStatusFilter(it) },
                            onDateFromChange = { remindersViewModel.updateDateFromFilter(it) },
                            onDateToChange = { remindersViewModel.updateDateToFilter(it) },
                            onSortByChange = { remindersViewModel.updateSortBy(it) },
                            onSortOrderChange = {},
                            filterConfig = remindersFilterConfig
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
                                    onDelete = { remindersViewModel.deleteReminder(reminder, sessionManager) },
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