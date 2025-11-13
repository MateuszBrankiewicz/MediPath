package com.medipath.modules.doctor.visit.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.models.Visit
import com.medipath.core.theme.MediPathTheme
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.doctor.visit.DoctorVisitViewModel
import com.google.gson.Gson

class DoctorVisitDetailsActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val visitJson = intent.getStringExtra("VISIT_JSON")
        val isCurrent = intent.getBooleanExtra("IS_CURRENT", false)
        
        if (visitJson == null) {
            Toast.makeText(this, "Error loading visit data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        val visit = Gson().fromJson(visitJson, Visit::class.java)
        
        setContent {
            MediPathTheme {
                DoctorVisitDetailsScreen(
                    visit = visit,
                    isCurrent = isCurrent,
                    onBackClick = { finish() },
                    onFinishVisit = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorVisitDetailsScreen(
    visit: Visit,
    isCurrent: Boolean,
    viewModel: DoctorVisitViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onFinishVisit: () -> Unit = {}
) {
    val medicalHistory by viewModel.medicalHistory.collectAsState()
    val prescriptionCodes by viewModel.prescriptionCodes.collectAsState()
    val referralCodes by viewModel.referralCodes.collectAsState()
    val noteText by viewModel.noteText.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingHistory by viewModel.isLoadingHistory.collectAsState()
    val visitCompleted by viewModel.visitCompleted.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val colors = LocalCustomColors.current
    val context = LocalContext.current
    
    val isEditable = isCurrent && visit.status != "Completed"
    val isCompleted = visit.status == "Completed"

    LaunchedEffect(visit) {
        viewModel.setVisit(visit, isCurrent)
    }
    
    LaunchedEffect(visitCompleted) {
        if (visitCompleted) {
            Toast.makeText(context, "Visit completed successfully", Toast.LENGTH_SHORT).show()
            onFinishVisit()
        }
    }
    
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${visit.patient.name} ${visit.patient.surname}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GovId: ${visit.patient.govID}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Return"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.blue900,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.Top
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isCompleted && !isEditable) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.green800
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Visit completed",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Medical history:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (isLoadingHistory) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp),
                                    color = colors.blue900
                                )
                            }
                        } else if (medicalHistory.isEmpty()) {
                            Text(
                                text = "No medical history available",
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        } else {
                            val historyScrollState = rememberScrollState()

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                                    .verticalScroll(historyScrollState)

                            ) {
                                medicalHistory.forEachIndexed { index, history ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.2f
                                            )
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = history.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = history.date,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.7f
                                            )
                                        )

                                        if (history.doctor != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Dr. ${history.doctor!!.doctorName} ${history.doctor!!.doctorSurname}",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.7f
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = history.note,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Prescriptions:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Enter prescription codes (separated by comma, space or new line)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = prescriptionCodes,
                            onValueChange = { if (isEditable) viewModel.setPrescriptionCodes(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            enabled = isEditable,
                            placeholder = { Text("e.g. PRE001, PRE002, PRE003") },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = !isEditable
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Referrals:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Enter referral codes (separated by comma, space or new line)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = referralCodes,
                            onValueChange = { if (isEditable) viewModel.setReferralCodes(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            enabled = isEditable,
                            placeholder = { Text("e.g. REF001, REF002, REF003") },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = !isEditable
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Notes:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { if (isEditable) viewModel.setNoteText(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            enabled = isEditable,
                            placeholder = { Text("Enter visit notes...") },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = !isEditable
                        )
                    }
                }
            }

            item {
                if (isEditable) {
                    Button(
                        onClick = {
                            viewModel.completeVisit()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.blue900
                        ),
                        shape = RoundedCornerShape(30.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "FINISH VISIT",
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
