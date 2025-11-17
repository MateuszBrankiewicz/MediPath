package com.medipath.modules.patient.medical_history.ui

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
import androidx.compose.material.icons.filled.Add
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
import com.medipath.modules.patient.medical_history.MedicalHistoryViewModel
import com.medipath.modules.patient.medical_history.ui.components.MedicalHistoryCard
import com.medipath.modules.shared.notifications.NotificationsViewModel
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.shared.components.FilterChipsConfig
import com.medipath.modules.shared.components.GenericFilterChipsSection
import com.medipath.modules.shared.components.GenericFilterToggleRow
import com.medipath.modules.shared.components.GenericSearchBar
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.profile.ui.EditProfileActivity
import com.medipath.modules.shared.settings.ui.SettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MedicalHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MediPathTheme {
                MedicalHistoryScreen(
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val authService = RetrofitInstance.authService
                            val sessionManager = RetrofitInstance.getSessionManager()
                            try {
                                authService.logout()
                            } catch (e: Exception) {
                                Log.e("MedicalHistoryActivity", "Logout API error", e)
                            }

                            sessionManager.deleteSessionId()

                            withContext(Dispatchers.Main) {
                                startActivity(
                                    Intent(this@MedicalHistoryActivity, LoginActivity::class.java)
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
fun MedicalHistoryScreen(
    viewModel: HomeViewModel = viewModel(),
    medicalHistoryViewModel: MedicalHistoryViewModel = viewModel(),
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = LocalCustomColors.current
    
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val canBeDoctor by viewModel.canBeDoctor.collectAsState()

    val histories by medicalHistoryViewModel.filteredMedicalHistories.collectAsState()
    val historiesLoading by medicalHistoryViewModel.isLoading.collectAsState()
    val historiesError by medicalHistoryViewModel.error.collectAsState()
    val deleteSuccess by medicalHistoryViewModel.deleteSuccess.collectAsState()
    val searchQuery by medicalHistoryViewModel.searchQuery.collectAsState()
    val sortBy by medicalHistoryViewModel.sortBy.collectAsState()
    val sortOrder by medicalHistoryViewModel.sortOrder.collectAsState()
    val totalHistories by medicalHistoryViewModel.totalHistories.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    val notificationsViewModel: NotificationsViewModel = viewModel()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchUserProfile()
                notificationsViewModel.fetchNotifications()
                medicalHistoryViewModel.fetchMedicalHistories()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(historiesError) {
        if (historiesError != null) {
            Toast.makeText(context, historiesError, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            Toast.makeText(context, "Medical history deleted successfully", Toast.LENGTH_SHORT).show()
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
        Navigation(
            notificationsViewModel = notificationsViewModel,
            screenTitle = "Medical History",
            canSwitchRole = canBeDoctor,
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
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(innerPadding)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        item {
                            GenericSearchBar(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { medicalHistoryViewModel.updateSearchQuery(it) },
                                placeholder = "Search by title, doctor or notes...",
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                        }

                        item {
                            GenericFilterToggleRow(
                                totalItems = totalHistories,
                                showingItems = histories.size,
                                showFilters = showFilters,
                                onToggleFilters = { showFilters = !showFilters }
                            )
                        }

                        if (showFilters) {
                            item {
                                GenericFilterChipsSection(
                                    sortBy = sortBy,
                                    sortOrder = sortOrder,
                                    onSortByChange = { medicalHistoryViewModel.updateSortBy(it) },
                                    onSortOrderChange = { medicalHistoryViewModel.updateSortOrder(it) },
                                    onClearFilters = { medicalHistoryViewModel.clearFilters() },
                                    config = FilterChipsConfig(
                                        sortByOptions = listOf("Date", "Title", "Doctor")
                                    )
                                )
                            }
                        }

                        if (historiesLoading) {
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
                        } else if (histories.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (totalHistories == 0) "No medical history yet" else "No entries match your filters",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        } else {
                            items(histories, key = { it.id }) { history ->
                                MedicalHistoryCard(
                                    history = history,
                                    onView = { historyId ->
                                        val intent = Intent(context, MedicalHistoryDetailsActivity::class.java)
                                        intent.putExtra("HISTORY_ID", historyId)
                                        intent.putExtra("IS_READ_ONLY", true)
                                        context.startActivity(intent)
                                    },
                                    onEdit = { historyId ->
                                        val intent = Intent(context, MedicalHistoryDetailsActivity::class.java)
                                        intent.putExtra("HISTORY_ID", historyId)
                                        intent.putExtra("IS_READ_ONLY", false)
                                        context.startActivity(intent)
                                    },
                                    onDelete = { historyId ->
                                        medicalHistoryViewModel.deleteMedicalHistory(historyId)
                                    }
                                )
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            val intent = Intent(context, MedicalHistoryDetailsActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = colors.blue900
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add medical history",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            },
            onLogoutClick = onLogoutClick,
            firstName = firstName,
            lastName = lastName,
            currentTab = "Medical history"
        )
    }
}
