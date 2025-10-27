package com.medipath.modules.patient.visits.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.visits.VisitDetailsViewModel
import com.medipath.modules.patient.visits.ui.components.VisitDetailsContent

class VisitDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val visitId = intent.getStringExtra("VISIT_ID") ?: ""
        val sessionManager = DataStoreSessionManager(this)

        setContent {
            MediPathTheme {
                VisitDetailsScreen(
                    visitId = visitId,
                    sessionManager = sessionManager,
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailsScreen(
    visitId: String,
    sessionManager: DataStoreSessionManager,
    viewModel: VisitDetailsViewModel = remember { VisitDetailsViewModel() },
    onBackClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val visitDetails by viewModel.visitDetails
    val isLoading by viewModel.isLoading
    val error by viewModel.error

    LaunchedEffect(visitId) {
        viewModel.fetchVisitDetails(visitId, sessionManager)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Visit Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Return",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.blue900
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.secondary)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                    }
                }

                visitDetails != null -> {
                    val context = LocalContext.current
                    VisitDetailsContent(
                        visitDetails = visitDetails!!,
                        onReviewClick = {
                            val intent = Intent(context, ReviewVisitActivity::class.java).apply {
                                putExtra("VISIT_ID", visitId)
                                putExtra(
                                    "DOCTOR_NAME",
                                    "Dr. ${visitDetails!!.doctor.doctorName} ${visitDetails!!.doctor.doctorSurname}"
                                )
                                putExtra(
                                    "INSTITUTION_NAME",
                                    visitDetails!!.institution.institutionName
                                )
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
