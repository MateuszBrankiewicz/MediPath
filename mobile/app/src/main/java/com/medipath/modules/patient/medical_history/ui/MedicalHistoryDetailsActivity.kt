package com.medipath.modules.patient.medical_history.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.R
import com.medipath.core.models.UserMedicalHistory
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.medical_history.MedicalHistoryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MedicalHistoryDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val historyId = intent.getStringExtra("HISTORY_ID")
        val isReadOnly = intent.getBooleanExtra("IS_READ_ONLY", false)

        setContent {
            MediPathTheme {
                MedicalHistoryDetailsScreen(
                    historyId = historyId,
                    isReadOnly = isReadOnly,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryDetailsScreen(
    historyId: String?,
    isReadOnly: Boolean,
    onBack: () -> Unit,
    viewModel: MedicalHistoryViewModel = viewModel()
) {
    val colors = LocalCustomColors.current
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var historyData by remember { mutableStateOf<UserMedicalHistory?>(null) }

    val addUpdateSuccess by viewModel.addUpdateSuccess.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val medicalHistories by viewModel.medicalHistories.collectAsState()

    LaunchedEffect(historyId) {
        if (historyId != null) {
            val history = viewModel.getMedicalHistoryById(historyId)
            
            if (history == null) {
                viewModel.fetchMedicalHistories()
            }
        }
    }

    LaunchedEffect(historyId, medicalHistories) {
        if (historyId != null) {
            val history = viewModel.getMedicalHistoryById(historyId)
            history?.let {
                title = it.title
                note = it.note
                date = it.date
                historyData = it
            }
        }
    }

    LaunchedEffect(addUpdateSuccess) {
        if (addUpdateSuccess) {
            val message = if (historyId == null) {
                context.getString(R.string.medical_history_added_successfully)
            } else {
                context.getString(R.string.medical_history_updated_successfully)
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onBack()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            historyId == null -> stringResource(R.string.add_medical_history)
                            isReadOnly -> stringResource(R.string.view_medical_history)
                            else -> stringResource(R.string.edit_medical_history)
                        },
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (isReadOnly && historyData?.doctor != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = colors.blue800.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.created_by_doctor),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.blue900
                                )
                                Text(
                                    text = "${historyData!!.doctor!!.doctorName} ${historyData!!.doctor!!.doctorSurname}",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (historyData!!.doctor!!.specialisations.isNotEmpty()) {
                                    Text(
                                        text = historyData!!.doctor!!.specialisations.joinToString(", "),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isReadOnly,
                                placeholder = { Text(stringResource(R.string.enter_title)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.blue900,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.2f),
                                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.primary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(0.5f),
                                    focusedContainerColor = MaterialTheme.colorScheme.background.copy(0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.date_of_visit),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedTextField(
                                value = try {
                                    LocalDate.parse(date).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                } catch (_: Exception) {
                                    date
                                },
                                onValueChange = {},
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isReadOnly) {
                                        val currentDate = try {
                                            LocalDate.parse(date)
                                        } catch (_: Exception) {
                                            LocalDate.now()
                                        }

                                        val cal = Calendar.getInstance()
                                        cal.set(
                                            currentDate.year,
                                            currentDate.monthValue - 1,
                                            currentDate.dayOfMonth
                                        )

                                        val dpd = DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val formatted = String.format(
                                                    "%04d-%02d-%02d",
                                                    year,
                                                    month + 1,
                                                    dayOfMonth
                                                )
                                                date = formatted
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH)
                                        )
                                        dpd.show()
                                    },
                                enabled = false,
                                readOnly = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = if(!isReadOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = if(!isReadOnly) MaterialTheme.colorScheme.onSurface.copy(0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledContainerColor = if(!isReadOnly) MaterialTheme.colorScheme.background.copy(0.5f) else MaterialTheme.colorScheme.surface,
                                    disabledTrailingIconColor = if (!isReadOnly) colors.blue900 else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = stringResource(R.string.select_date),
                                        tint = if (!isReadOnly) colors.blue900 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }
                    }

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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.notes),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedTextField(
                                value = note,
                                onValueChange = { note = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                enabled = !isReadOnly,
                                placeholder = { Text(stringResource(R.string.enter_your_notes)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.blue900,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.3f),
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(0.2f),
                                    unfocusedTextColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.primary,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(0.5f),
                                    focusedContainerColor = MaterialTheme.colorScheme.background.copy(0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    if (error != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = error ?: stringResource(R.string.unknown_error),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    if (!isReadOnly) {
                        Button(
                            onClick = {
                                if (title.isNotBlank() && note.isNotBlank()) {
                                    if (historyId == null) {
                                        viewModel.addMedicalHistory(title, note, date)
                                    } else {
                                        viewModel.updateMedicalHistory(historyId, title, note, date)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            enabled = title.isNotBlank() && note.isNotBlank() && !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.blue900,
                                disabledContainerColor = colors.blue900.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (historyId == null) stringResource(R.string.save) else stringResource(
                                        R.string.update
                                    ),
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.blue900
                            ),
                            shape = RoundedCornerShape(30.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.close_capitals),
                                fontSize = 16.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
