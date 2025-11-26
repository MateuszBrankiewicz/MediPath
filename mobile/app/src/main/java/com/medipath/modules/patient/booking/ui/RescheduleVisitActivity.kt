package com.medipath.modules.patient.booking.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.medipath.R
import com.medipath.core.models.DoctorScheduleItem
import com.medipath.core.models.Institution
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.core.utils.LocaleHelper
import com.medipath.modules.patient.booking.RescheduleViewModel
import com.medipath.modules.patient.booking.ui.components.TimeSlotCard
import com.medipath.modules.shared.components.CalendarCard
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RescheduleVisitActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val visitId = intent.getStringExtra("visit_id") ?: ""
        val doctorId = intent.getStringExtra("doctor_id") ?: ""
        val doctorName = intent.getStringExtra("doctor_name") ?: ""
        val currentDate = intent.getStringExtra("current_date") ?: ""
        val isDoctor = intent.getBooleanExtra("is_doctor", false)

        setContent {
            MediPathTheme {
                RescheduleVisitScreen(
                    visitId = visitId,
                    doctorId = doctorId,
                    doctorName = doctorName,
                    currentDate = currentDate,
                    isDoctor = isDoctor,
                    onBackClick = { finish() },
                    onRescheduleSuccess = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleVisitScreen(
    visitId: String,
    doctorId: String,
    doctorName: String,
    currentDate: String,
    isDoctor: Boolean = false,
    onBackClick: () -> Unit,
    onRescheduleSuccess: () -> Unit
) {
    val context = LocalContext.current
    val locale = LocaleHelper.getLocale(context)
    val viewModel: RescheduleViewModel = viewModel()
    val schedules by viewModel.schedules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val rescheduleSuccess by viewModel.rescheduleSuccess.collectAsState()
    val patientInfo by viewModel.patientInfo.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.successMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(visitId, isDoctor) {
        if (isDoctor) {
            viewModel.loadVisitDetails(visitId)
        }
    }

    var selectedInstitution by remember { mutableStateOf<Institution?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedSchedule by remember { mutableStateOf<DoctorScheduleItem?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }

    val institutions = remember(schedules) {
        schedules.map { it.institution }.distinctBy { it.institutionId }
    }

    val filteredSchedules = remember(schedules, selectedInstitution) {
        if (selectedInstitution == null) schedules
        else schedules.filter { it.institution.institutionId == selectedInstitution?.institutionId }
    }

    val schedulesByDate = remember(filteredSchedules) {
        filteredSchedules
            .groupBy { 
                LocalDateTime.parse(it.startHour, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .toLocalDate()
            }
            .toSortedMap()
    }
    
    val dateCountMap = remember(schedulesByDate) {
        schedulesByDate.mapValues { it.value.size }
    }
    
    val availableDates = remember(schedulesByDate) {
        schedulesByDate.keys.toSet()
    }
    
    val schedulesForSelectedDate = remember(selectedDate, schedulesByDate) {
        selectedDate?.let { schedulesByDate[it] } ?: emptyList()
    }

    LaunchedEffect(doctorId) {
        viewModel.loadSchedules(doctorId)
    }

    LaunchedEffect(rescheduleSuccess) {
        if (rescheduleSuccess) {
            onRescheduleSuccess()
        }
    }

    if (showConfirmDialog && selectedSchedule != null) {

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.confirm_reschedule)) },
            text = {
                Column {
                    val schedule = selectedSchedule!!
                    val newDateTime = LocalDateTime.parse(schedule.startHour, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    
                    Text(stringResource(R.string.current_appointment), fontWeight = FontWeight.Bold)
                    Text(currentDate)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.new_appointment), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.doctor_label,doctorName))
                    Text(stringResource(R.string.institution_label,schedule.institution.institutionName))
                    Text(stringResource(R.string.date_label,newDateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", locale))))
                    Text(stringResource(R.string.time,newDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rescheduleVisit(visitId, selectedSchedule!!.id)
                        showConfirmDialog = false
                    },
                    enabled = !isLoading
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.reschedule_visit),
                            color = MaterialTheme.colorScheme.background,
                            fontSize = 23.sp
                        )
                        if (isDoctor && patientInfo != null) {
                            Text(
                                text = stringResource(
                                    R.string.patient_info,
                                    patientInfo!!.name,
                                    patientInfo!!.surname
                                ),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                            )
                            Text(
                                text = stringResource(R.string.gov_id) + patientInfo!!.govID,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                            )
                        } else {
                            Text(
                                text = doctorName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.background
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LocalCustomColors.current.blue900
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.secondary)
        ) {
            when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            schedules.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = stringResource(R.string.no_available_appointments),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.please_check_back_later),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (institutions.size > 1) {
                        item {
                            Text(
                                text = stringResource(R.string.select_institution),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        item {
                            ScrollableTabRow(
                                selectedTabIndex = institutions.indexOf(selectedInstitution).takeIf { it >= 0 } ?: 0,
                                edgePadding = 0.dp,
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                institutions.forEach { institution ->
                                    Tab(
                                        selected = selectedInstitution?.institutionId == institution.institutionId,
                                        onClick = { 
                                            selectedInstitution = institution
                                            selectedDate = null
                                            selectedSchedule = null
                                        },
                                        text = {
                                            Text(
                                                text = institution.institutionName,
                                                fontSize = 14.sp
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.select_new_date),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    item {
                        CalendarCard(
                            currentMonth = currentMonth,
                            availableDates = availableDates,
                            dateCountMap = dateCountMap,
                            selectedDate = selectedDate,
                            onMonthChange = { newMonth ->
                                currentMonth = newMonth
                            },
                            onDateSelected = { date ->
                                selectedDate = date
                                selectedSchedule = null
                            },
                        )
                    }

                    if (selectedDate != null && schedulesForSelectedDate.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.available_times),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }

                        item {
                            Text(
                                text = selectedDate!!.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", locale)),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        schedulesForSelectedDate.chunked(3).forEach { scheduleRow ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    scheduleRow.forEach { schedule ->
                                        TimeSlotCard(
                                            schedule = schedule,
                                            isSelected = selectedSchedule?.id == schedule.id,
                                            onClick = { 
                                                if (!schedule.booked) {
                                                    selectedSchedule = schedule
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(3 - scheduleRow.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else if (selectedDate != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.no_available_appointments_for_this_date),
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    if (selectedSchedule != null) {
                        item {
                            Button(
                                onClick = { showConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(30.dp),
                                enabled = !isLoading
                            ) {
                                Text(stringResource(R.string.reschedule_visit_capitals), fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
        }
    }
}
