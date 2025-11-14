package com.medipath.modules.doctor.visits.ui

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
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.doctor.visits.DoctorVisitsViewModel
import com.medipath.modules.doctor.visit.ui.DoctorVisitDetailsActivity
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.FilterChipsConfig
import com.medipath.modules.shared.components.GenericFilterChipsSection
import com.medipath.modules.shared.components.GenericFilterToggleRow
import com.medipath.modules.shared.components.GenericSearchBar
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.profile.ProfileViewModel
import com.medipath.modules.shared.settings.ui.SettingsActivity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DoctorVisitsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MediPathTheme {
                DoctorVisitsScreen(
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val authService = RetrofitInstance.authService
                            val sessionManager = RetrofitInstance.getSessionManager()
                            try {
                                authService.logout()
                            } catch (e: Exception) {
                                Log.e("DoctorVisitsActivity", "Logout API error", e)
                            }
                            sessionManager.deleteSessionId()
                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(this@DoctorVisitsActivity, LoginActivity::class.java)
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
fun DoctorVisitsScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    visitsViewModel: DoctorVisitsViewModel = viewModel(),
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    
    val name by profileViewModel.name.collectAsState()
    val surname by profileViewModel.surname.collectAsState()
    val isProfileLoading by profileViewModel.isLoading.collectAsState()

    val visits by visitsViewModel.filteredVisits.collectAsState()
    val visitsLoading by visitsViewModel.isLoading.collectAsState()
    val visitsError by visitsViewModel.error.collectAsState()
    val searchQuery by visitsViewModel.searchQuery.collectAsState()
    val statusFilter by visitsViewModel.statusFilter.collectAsState()
    val sortOrder by visitsViewModel.sortOrder.collectAsState()
    val totalVisits by visitsViewModel.totalVisits.collectAsState()
    val shouldRedirectToLogin by visitsViewModel.shouldRedirectToLogin.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    val notificationsViewModel: NotificationsViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                profileViewModel.fetchProfile()
                notificationsViewModel.fetchNotifications()
                visitsViewModel.fetchVisits()
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

    if (shouldRedirectToLogin) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

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
    } else if (isProfileLoading || visitsLoading) {
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
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(innerPadding)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            GenericSearchBar(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { visitsViewModel.updateSearchQuery(it) },
                                placeholder = "Search by patient name, surname or GovID...",
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                        }

                        item {
                            GenericFilterToggleRow(
                                totalItems = totalVisits,
                                showingItems = visits.size,
                                showFilters = showFilters,
                                onToggleFilters = { showFilters = !showFilters }
                            )
                        }

                        if (showFilters) {
                            item {
                                GenericFilterChipsSection(
                                    sortBy = statusFilter,
                                    sortOrder = sortOrder,
                                    onSortByChange = { visitsViewModel.updateStatusFilter(it) },
                                    onSortOrderChange = { visitsViewModel.updateSortOrder(it) },
                                    onClearFilters = { visitsViewModel.clearFilters() },
                                    config = FilterChipsConfig(
                                        sortByOptions = listOf("All", "Upcoming", "Completed", "Cancelled"),
                                        sortOrderLabel = "Order by date"
                                    )
                                )
                            }
                        }

                        if (visitsLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else if (visits.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (totalVisits == 0) "No visits yet" else "No visits match your filters",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            items(visits, key = { it.id }) { visit ->
                                DoctorVisitCard(
                                    visit = visit,
                                    onViewDetails = {
                                        val intent = Intent(context, DoctorVisitDetailsActivity::class.java)
                                        intent.putExtra("VISIT_JSON", Gson().toJson(visit))
                                        intent.putExtra("IS_CURRENT", false)
                                        context.startActivity(intent)
                                    },
                                    onCancel = {}
                                )
                            }
                        }
                    }
                }
            },
            onLogoutClick = onLogoutClick,
            firstName = name,
            lastName = surname,
            currentTab = "Visits",
            isDoctor = true,
            canSwitchRole = true
        )
    }
}
