package com.medipath.modules.patient.visits.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.core.models.Code
import com.medipath.modules.patient.visits.VisitDetailsViewModel

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
                                putExtra("DOCTOR_NAME", "Dr. ${visitDetails!!.doctor.doctorName} ${visitDetails!!.doctor.doctorSurname}")
                                putExtra("INSTITUTION_NAME", visitDetails!!.institution.institutionName)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VisitDetailsContent(
    visitDetails: com.medipath.core.models.VisitDetails,
    onReviewClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        val statusColor = when (visitDetails.status.lowercase()) {
            "upcoming" -> colors.orange800
            "cancelled" -> colors.red800
            "completed" -> colors.green800
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = visitDetails.status,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        InfoCard(title = "Doctor") {
            InfoRow("Name", "Dr. ${visitDetails.doctor.doctorName} ${visitDetails.doctor.doctorSurname}")
            InfoRow("Specializations", visitDetails.doctor.specialisations.joinToString(", "))
        }

        InfoCard(title = "Patient") {
            InfoRow("Name", "${visitDetails.patient.name} ${visitDetails.patient.surname}")
            InfoRow("Government ID", visitDetails.patient.govID)
        }

        InfoCard(title = "Visit Time") {
            InfoRow("Start", visitDetails.time.startTime)
            InfoRow("End", visitDetails.time.endTime)
        }

        InfoCard(title = "Institution") {
            InfoRow("Name", visitDetails.institution.institutionName)
        }

        if (visitDetails.note.isNotEmpty()) {
            InfoCard(title = "Doctor's Note") {
                Text(
                    text = visitDetails.note,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (visitDetails.patientRemarks.isNotEmpty()) {
            InfoCard(title = "Patient Remarks") {
                Text(
                    text = visitDetails.patientRemarks,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        val prescriptions = visitDetails.codes.filter { it.codeType == "PRESCRIPTION" }
        if (prescriptions.isNotEmpty()) {
            CodesCard(title = "Prescriptions", codes = prescriptions, color = colors.blue800)
        }

        val referrals = visitDetails.codes.filter { it.codeType == "REFERRAL" }
        if (referrals.isNotEmpty()) {
            CodesCard(title = "Referrals", codes = referrals, color = colors.orange800)
        }
        }

        if (visitDetails.status.lowercase() == "completed") {
            Button(
                onClick = onReviewClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.blue900
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "REVIEW VISIT",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
fun CodesCard(title: String, codes: List<Code>, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            
            codes.forEach { code ->
                CodeItem(code = code, color = color)
            }
        }
    }
}

@Composable
fun CodeItem(code: Code, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Code: ${code.code}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
        Text(
            text = if (code.active) "Active" else "Inactive",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (code.active) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
            modifier = Modifier
                .background(
                    color = if (code.active) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFF9E9E9E).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
