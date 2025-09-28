package com.medipath.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medipath.ui.theme.MediPathTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MedicalInformation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.medipath.data.api.DataStoreSessionManager
import com.medipath.data.api.RetrofitInstance
import com.medipath.ui.auth.LoginActivity
import com.medipath.ui.codes.CodesActivity
import com.medipath.ui.components.InfoCard
import com.medipath.ui.components.MenuCard
import com.medipath.ui.components.Navigation
import com.medipath.ui.components.SearchBar
import com.medipath.ui.components.VisitItem
import com.medipath.ui.theme.LocalCustomColors
import com.medipath.viewmodels.HomeViewModel
import kotlinx.coroutines.launch

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sessionManager = DataStoreSessionManager(this)
        val apiService = RetrofitInstance.api
        setContent {
            MediPathTheme {
                HomeScreen(
                    onLogoutClick = {
                        lifecycleScope.launch {
                            try {
                                apiService.logout()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                sessionManager.deleteSessionId()
                                startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                                finish()
                            }
                        }
                    },
                    sessionManager = sessionManager
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit = {},
    viewModel: HomeViewModel = remember { HomeViewModel() },
    sessionManager: DataStoreSessionManager
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile(sessionManager)
    }

    val firstName by viewModel.firstName
    val upcomingVisits by viewModel.upcomingVisits
    val deleteSuccess by viewModel.deleteSuccess
    val deleteError by viewModel.deleteError
    val prescriptionCode by viewModel.prescriptionCode
    val referralCode by viewModel.referralCode

    Navigation(
        content = { innerPadding ->
            val colors = LocalCustomColors.current
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(Modifier.fillMaxSize()) {
                    SearchBar()
                    Column(
                        modifier = Modifier
                            .padding(vertical = 20.dp, horizontal = 30.dp)
                    ) {
                        Text(
                            text = "Dashboard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        InfoCard(
                            title = "Prescriptions",
                            label = "Code:",
                            code = if (prescriptionCode.isNotEmpty()) prescriptionCode else "No active prescriptions",
                            onClick = {
                                val intent = Intent(context, CodesActivity::class.java)
                                intent.putExtra("code_type", "PRESCRIPTION")
                                intent.putExtra("user_id", viewModel.getCurrentUserId())
                                context.startActivity(intent)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        InfoCard(
                            title = "Referrals",
                            label = "Code:",
                            code = if(referralCode.isNotEmpty()) referralCode else "No active referrals",
                            onClick = {
                                val intent = Intent(context, CodesActivity::class.java)
                                intent.putExtra("code_type", "REFERRAL")
                                intent.putExtra("user_id", viewModel.getCurrentUserId())
                                context.startActivity(intent)
                            }
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MenuCard(
                                icon = Icons.AutoMirrored.Outlined.List,
                                title = "Visits",
                                onClick = { println("Visits clicked!") },
                                backgroundColor = colors.purple800,
                                iconColor = colors.purple300
                            )

                            MenuCard(
                                icon = Icons.Outlined.MedicalInformation,
                                title = "Medical History",
                                onClick = { println("History clicked!") },
                                backgroundColor = colors.blue800,
                                iconColor = colors.blue300
                            )

                            MenuCard(
                                icon = Icons.AutoMirrored.Outlined.Comment,
                                title = "Opinions",
                                onClick = { println("Opinions clicked!") },
                                backgroundColor = colors.orange800,
                                iconColor = colors.orange300
                            )

                            MenuCard(
                                icon = Icons.Outlined.Notifications,
                                title = "Reminders",
                                onClick = { println("Reminders clicked!") },
                                backgroundColor = colors.green800,
                                iconColor = colors.green300
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        ) {
                            Text(
                                "Upcoming visits",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Visit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        HorizontalDivider(thickness = 2.dp)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 450.dp)
                        ) {
                            if(upcomingVisits.isEmpty())
                                item {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        "No upcoming visits",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            else
                                items(upcomingVisits.size) { index ->
                                    VisitItem(
                                        visit = upcomingVisits[index],
                                        onCancelVisit = { visitId ->
                                            viewModel.cancelVisit(visitId, sessionManager)
                                        }
                                    )
                                    HorizontalDivider()
                                }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        },
        onNotificationsClick = {
            //powiadomienia
        },
        onLogoutClick = onLogoutClick,
        firstName = firstName
    )

    if (deleteSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteMessages() },
            title = { Text("Sukces") },
            text = { Text("Wizyta została pomyślnie anulowana") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearDeleteMessages() }) {
                    Text("OK")
                }
            }
        )
    }

    if (deleteError.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteMessages() },
            title = { Text("Błąd") },
            text = { Text(deleteError) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearDeleteMessages() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
//    MediPathTheme { HomeScreen() }
}