package com.medipath.modules.patient.codes.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import com.medipath.core.network.DataStoreSessionManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.codes.CodesViewModel
import com.medipath.modules.patient.home.HomeViewModel
import com.medipath.modules.patient.notifications.ui.NotificationsActivity
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CodesActivity : ComponentActivity() {
    private var currentCodeType = mutableStateOf("PRESCRIPTION")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val codeType = intent.getStringExtra("code_type") ?: "PRESCRIPTION"
        currentCodeType.value = codeType
        val sessionManager = DataStoreSessionManager(this)

        setContent {
            MediPathTheme {
                CodesScreen(
                    sessionManager = sessionManager,
                    codeType = currentCodeType.value,
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                sessionManager.deleteSessionId()
                                withContext(Dispatchers.Main) {
                                    startActivity(
                                        Intent(
                                            this@CodesActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@CodesActivity,
                                        "Logout error",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val codeType = intent.getStringExtra("code_type") ?: "PRESCRIPTION"
        currentCodeType.value = codeType
    }
}

@Composable
fun CodesScreen(
    sessionManager: DataStoreSessionManager,
    profileViewModel: HomeViewModel = remember { HomeViewModel() },
    onLogoutClick: () -> Unit,
    codeType: String
) {
    val codesViewModel: CodesViewModel = viewModel()
    val codes by codesViewModel.codes
    val isLoading by codesViewModel.isLoading
    val error by codesViewModel.error
    val successMessage by codesViewModel.successMessage
    val clipboardManager = LocalClipboardManager.current
    var copiedCode by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firstName by profileViewModel.firstName
    val lastName by profileViewModel.lastName
    val scope = rememberCoroutineScope()
    val colors = LocalCustomColors.current

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var dateFromFilter by remember { mutableStateOf("") }
    var dateToFilter by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Date") }
    var sortOrder by remember { mutableStateOf("Descending") }
    var showFilters by remember { mutableStateOf(false) }
    val label = if (codeType == "PRESCRIPTION") "prescriptions" else "referrals"

    LaunchedEffect(codeType) {
        val sessionToken = sessionManager.getSessionId()
        if (!sessionToken.isNullOrEmpty()) {
            codesViewModel.fetchCodes(sessionToken, label)
            profileViewModel.fetchUserProfile(sessionManager)
        }
    }

    LaunchedEffect(copiedCode) {
        if (copiedCode.isNotEmpty()) {
            delay(2000)
            copiedCode = ""
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage.isNotEmpty()) {
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            delay(2000)
            codesViewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            delay(2000)
            codesViewModel.clearError()
        }
    }

    val filteredCodes = codes
        .filter { it.codes.codeType == codeType }
        .filter { codeItem ->
            when (statusFilter) {
                "Unused" -> codeItem.codes.isActive
                "Used" -> !codeItem.codes.isActive
                else -> true
            }
        }
        .filter { codeItem ->
            if (searchQuery.isEmpty()) true
            else {
                codeItem.codes.code.contains(searchQuery, ignoreCase = true) ||
                codeItem.doctor.contains(searchQuery, ignoreCase = true)
            }
        }
        .filter { codeItem ->
            if (dateFromFilter.isEmpty()) true
            else codeItem.date >= dateFromFilter
        }
        .filter { codeItem ->
            if (dateToFilter.isEmpty()) true
            else codeItem.date <= dateToFilter
        }
        .let { list ->
            when (sortBy) {
                "Date" -> if (sortOrder == "Ascending") 
                    list.sortedBy { it.date }
                else 
                    list.sortedByDescending { it.date }
                "Doctor" -> if (sortOrder == "Ascending")
                    list.sortedBy { it.doctor }
                else
                    list.sortedByDescending { it.doctor }
                "Code" -> if (sortOrder == "Ascending")
                    list.sortedBy { it.codes.code }
                else
                    list.sortedByDescending { it.codes.code }
                else -> list
            }
        }
    
    val allCodesOfType = codes.filter { it.codes.codeType == codeType }
    val totalCodes = allCodesOfType.size
    val usedCodes = allCodesOfType.count { !it.codes.isActive }
    val unusedCodes = allCodesOfType.count { it.codes.isActive }

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
                icon = Icons.Default.Refresh,
                label = "REFRESH",
                onClick = {
                    scope.launch {
                        val sessionToken = sessionManager.getSessionId()
                        if (!sessionToken.isNullOrEmpty()) {
                            val apiCodeType = when (codeType) {
                                "PRESCRIPTION" -> "prescriptions"
                                "REFERRAL" -> "referrals"
                                else -> null
                            }
                            codesViewModel.fetchCodes(sessionToken, apiCodeType)
                        } } },
                color = colors.blue800,
                isOutlined = true
            )
        )
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
            screenTitle = if (codeType == "PRESCRIPTION") "Prescriptions" else "Referrals",
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
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    GenericStatisticsCards(
                        statistics = listOf(
                            StatisticItem(
                                icon = if (codeType == "PRESCRIPTION") Icons.Default.Receipt else Icons.Default.MedicalServices,
                                iconTint = colors.blue800,
                                label = "Total\n$label",
                                value = totalCodes.toString(),
                                valueTint = colors.blue800
                            ),
                            StatisticItem(
                                icon = Icons.Default.CheckCircle,
                                iconTint = colors.orange800,
                                label = "Used\n$label",
                                value = usedCodes.toString(),
                                valueTint = colors.orange800
                            ),
                            StatisticItem(
                                icon = Icons.Default.Circle,
                                iconTint = colors.green800,
                                label = "Unused\n$label",
                                value = unusedCodes.toString(),
                                valueTint = colors.green800
                            )
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    GenericActionButtonsRow(
                        buttons = actionButtons,
                        buttonsPerRow = 2
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    GenericSearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        placeholder = "Search by code, doctor..."
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (showFilters) {
                        GenericFiltersSection(
                            statusFilter = statusFilter,
                            dateFromFilter = dateFromFilter,
                            dateToFilter = dateToFilter,
                            sortBy = sortBy,
                            sortOrder = sortOrder,
                            onStatusFilterChange = { statusFilter = it },
                            onDateFromChange = { dateFromFilter = it },
                            onDateToChange = { dateToFilter = it },
                            onSortByChange = { sortBy = it },
                            onSortOrderChange = { sortOrder = it },
                            filterConfig = FilterConfig(
                                statusOptions = listOf("All", "Unused", "Used"),
                                sortByOptions = listOf("Date", "Doctor", "Code"),
                                showSortOrder = true
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    if (filteredCodes.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                        Icon(
                            imageVector = if (codeType == "PRESCRIPTION") Icons.Default.Receipt else Icons.Default.MedicalServices,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (codeType == "PRESCRIPTION") "No prescriptions found" else "No referrals found",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            items(filteredCodes) { codeItem ->
                            CodeCard(
                                codeItem = codeItem,
                                onCopyClick = { code ->
                                    clipboardManager.setText(AnnotatedString(code))
                                    copiedCode = code
                                },
                                onMarkAsUsedClick = {
                                    scope.launch {
                                        val sessionToken = sessionManager.getSessionId()
                                        if (!sessionToken.isNullOrEmpty()) {
                                            codesViewModel.markCodeAsUsed(
                                                sessionToken,
                                                codeItem.codes.codeType,
                                                codeItem.codes.code
                                            )
                                        }
                                    }
                                },
                                onDeleteClick = {
                                    scope.launch {
                                        val sessionToken = sessionManager.getSessionId()
                                        if (!sessionToken.isNullOrEmpty()) {
                                            codesViewModel.deleteCode(
                                                sessionToken,
                                                codeItem.codes.codeType,
                                                codeItem.codes.code
                                            )
                                        }
                                    }
                                },
                                isCopied = copiedCode == codeItem.codes.code
                            )
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                        }
                    }
                }
            },
            onLogoutClick = onLogoutClick,
            firstName = firstName,
            lastName = lastName,
            currentTab = if (codeType == "PRESCRIPTION") "Prescriptions" else "Referrals"
        )
    }

    if (copiedCode.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Code copied to clipboard",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp
            )
        }
    }
}