package com.medipath.modules.shared.reminders.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.modules.shared.reminders.RemindersViewModel
import com.medipath.modules.shared.reminders.ui.components.ReminderCard
import com.medipath.modules.shared.reminders.ui.components.TabSelector
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isDoctor = intent.getBooleanExtra("isDoctor", false)

        setContent {
            MediPathTheme {

                RemindersScreen(
                    isDoctor = isDoctor,
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val authService = RetrofitInstance.authService
                            val sessionManager = RetrofitInstance.getSessionManager()
                            try {
                                authService.logout()
                            } catch (e: Exception) {
                                Log.e("RemindersActivity", "Logout API error", e)
                            }
                            sessionManager.deleteSessionId()
                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(this@RemindersActivity, LoginActivity::class.java)
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
fun RemindersScreen(
    refreshTrigger: Int = 0,
    profileViewModel: HomeViewModel = viewModel(),
    remindersViewModel: RemindersViewModel = viewModel(),
    isDoctor: Boolean = false,
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val firstName by profileViewModel.firstName.collectAsState()
    val lastName by profileViewModel.lastName.collectAsState()
    val isProfileLoading by profileViewModel.isLoading.collectAsState()
    val profileShouldRedirect by profileViewModel.shouldRedirectToLogin.collectAsState()
    val roleCode by profileViewModel.roleCode.collectAsState()
    val canBeDoctor by profileViewModel.canBeDoctor.collectAsState()
    val colors = LocalCustomColors.current

    val reminders by remindersViewModel.filteredReminders.collectAsState()
    val isLoading by remindersViewModel.isLoading.collectAsState()
    val remindersShouldRedirect by remindersViewModel.shouldRedirectToLogin.collectAsState()
    val totalReminders by remindersViewModel.totalReminders.collectAsState()
    val unreadReminders by remindersViewModel.unreadReminders.collectAsState()
    val todayReminders by remindersViewModel.todayReminders.collectAsState()

    val statusFilter by remindersViewModel.statusFilter.collectAsState()
    val dateFromFilter by remindersViewModel.dateFromFilter.collectAsState()
    val dateToFilter by remindersViewModel.dateToFilter.collectAsState()
    val sortBy by remindersViewModel.sortBy.collectAsState()
    val sortOrder by remindersViewModel.sortOrder.collectAsState()
    val searchQuery by remindersViewModel.searchQuery.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Received") }

    val notificationsViewModel: NotificationsViewModel = viewModel()

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

    val displayedStatistics = remember(statisticsItems, selectedTab) {
        if (selectedTab == "Scheduled") {
            listOf(statisticsItems.first())
        } else {
            statisticsItems
        }
    }

    val actionButtons = mutableListOf(
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
            onClick = {
                val filter = when (selectedTab) {
                    "Received" -> "received"
                    "Scheduled" -> "upcoming"
                    else -> "received"
                }
                remindersViewModel.updateSelectedTab(filter)
                remindersViewModel.fetchReminders()
            },
            color = colors.blue800,
            isOutlined = true
        ),
    )

    if (selectedTab != "Scheduled") {
        actionButtons.add(
            ActionButton(
                icon = Icons.Default.DoneAll,
                label = "MARK ALL",
                onClick = {
                    remindersViewModel.markAllAsRead(onSuccess = {
                        notificationsViewModel.fetchNotifications()
                    })
                },
                color = colors.blue900,
                isOutlined = false
            )
        )
    }
    actionButtons.add(
        ActionButton(
            icon = Icons.Default.FilterList,
            label = "FILTERS",
            onClick = { showFilters = !showFilters },
            color = colors.blue800,
            isOutlined = true
        )
    )
    
    actionButtons.add(
        ActionButton(
            icon = Icons.Default.Clear,
            label = "CLEAR FILTERS",
            onClick = { remindersViewModel.clearFilters() },
            color = colors.error,
            isOutlined = true
        )
    )

    val remindersFilterConfig = remember {
        FilterConfig(
            statusOptions = listOf("All", "Read", "Unread"),
            sortByOptions = listOf("Date", "Title"),
            sortOrderOptions = listOf("Ascending", "Descending"),
            showSortOrder = true
        )
    }

    fun tabToFilter(tab: String): String? {
        return when (tab) {
            "Received" -> "received"
            "Scheduled" -> "upcoming"
            else -> null
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                profileViewModel.fetchUserProfile()
                remindersViewModel.fetchReminders()
                notificationsViewModel.fetchNotifications()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(selectedTab) {
        val filter = tabToFilter(selectedTab) ?: "received"
        remindersViewModel.updateSelectedTab(filter)
        remindersViewModel.fetchReminders()
    }

    val shouldRedirect = profileShouldRedirect || remindersShouldRedirect
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
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(innerPadding)
                ) {
                    GenericStatisticsCards(
                        statistics = displayedStatistics,
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
                            sortOrder = sortOrder,
                            onStatusFilterChange = { remindersViewModel.updateStatusFilter(it) },
                            onDateFromChange = { remindersViewModel.updateDateFromFilter(it) },
                            onDateToChange = { remindersViewModel.updateDateToFilter(it) },
                            onSortByChange = { remindersViewModel.updateSortBy(it) },
                            onSortOrderChange = { remindersViewModel.updateSortOrder(it) },
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
                                    onDelete = { remindersViewModel.deleteReminder(reminder) },
                                    onMarkAsRead = {
                                        remindersViewModel.markAsRead(reminder, onSuccess = {
                                            notificationsViewModel.fetchNotifications()
                                        })
                                    },
                                    showMark = (selectedTab == "Received")
                                )
                            }
                        }
                    }
                }
            },
            onLogoutClick = onLogoutClick,
            firstName = firstName,
            lastName = lastName,
            currentTab = "Reminders",
            isDoctor = isDoctor,
            canSwitchRole = canBeDoctor
        )
    }
}