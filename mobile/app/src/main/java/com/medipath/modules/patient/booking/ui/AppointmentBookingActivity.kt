package com.medipath.modules.patient.booking.ui

import android.os.Bundle
import com.medipath.core.models.Institution
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.medipath.R
import com.medipath.core.models.DoctorScheduleItem
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme
import com.medipath.modules.patient.booking.BookingViewModel
import com.medipath.modules.patient.booking.DoctorCommentsViewModel
import com.medipath.modules.patient.booking.ui.components.BookingTabContent
import com.medipath.modules.patient.booking.ui.components.InformationTab
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppointmentBookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val doctorId = intent.getStringExtra("doctor_id") ?: ""
        val doctorName = intent.getStringExtra("doctor_name") ?: ""
        val doctorImage = intent.getStringExtra("doctor_image") ?: ""
        val doctorRating = intent.getDoubleExtra("doctor_rating", 0.0)
        val numOfRatings = intent.getIntExtra("num_of_ratings", 0)
        val specialisations = intent.getStringExtra("specialisations") ?: ""

        setContent {
            MediPathTheme {
                AppointmentBookingScreen(
                    doctorId = doctorId,
                    doctorName = doctorName,
                    doctorImage = doctorImage,
                    doctorRating = doctorRating,
                    numOfRatings = numOfRatings,
                    specialisations = specialisations,
                    onBackClick = { finish() },
                    onBookingSuccess = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentBookingScreen(
    doctorId: String,
    doctorName: String,
    doctorImage: String = "",
    doctorRating: Double = 0.0,
    numOfRatings: Int = 0,
    specialisations: String = "",
    onBackClick: () -> Unit,
    onBookingSuccess: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.information),
        stringResource(R.string.book_appointment_capital)
    )

    val viewModel: BookingViewModel = viewModel()
    val schedules by viewModel.schedules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val bookingSuccess by viewModel.bookingSuccess.collectAsState()

    val commentsViewModel: DoctorCommentsViewModel = viewModel()

    val comments by commentsViewModel.comments.collectAsState()
    val isLoadingComments by commentsViewModel.isLoading.collectAsState()

    LaunchedEffect(doctorId) {
        viewModel.loadSchedules(doctorId)
        commentsViewModel.loadComments(doctorId)
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.successMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(bookingSuccess) {
        if (bookingSuccess) {
            onBookingSuccess()
        }
    }

    var selectedInstitution by remember { mutableStateOf<Institution?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedSchedule by remember { mutableStateOf<DoctorScheduleItem?>(null) }
    var patientNotes by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(LocalDate.now()) }

    val institutions = remember(schedules) {
        schedules.map { it.institution }.distinctBy { it.institutionId }
    }

    if (showConfirmDialog && selectedSchedule != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.confirm_appointment)) },
            text = {
                Column {
                    val schedule = selectedSchedule!!
                    val dateTime = LocalDateTime.parse(schedule.startHour, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    
                    Text(stringResource(R.string.doctor_label, doctorName))
                    Text(stringResource(R.string.institution_label,schedule.institution.institutionName))
                    Text(stringResource(R.string.date,dateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))))
                    Text(stringResource(R.string.time,dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))))
                    if (patientNotes.isNotEmpty()) {
                        Text(stringResource(R.string.notes_label,patientNotes))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.bookAppointment(selectedSchedule!!.id, patientNotes.ifEmpty { null })
                        showConfirmDialog = false
                    }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = stringResource(R.string.doctor_details),
                                color = MaterialTheme.colorScheme.background,
                                fontSize = 23.sp
                            )
                            Text(
                                text = doctorName,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                            )
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
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                when (selectedTab) {
                    0 -> InformationTab(
                        doctorName = doctorName,
                        doctorImage = doctorImage,
                        doctorRating = doctorRating,
                        numOfRatings = numOfRatings,
                        specialisations = specialisations,
                        institutions = institutions,
                        comments = comments,
                        isLoading = isLoadingComments
                    )

                    1 -> BookingTabContent(
                        schedules = schedules,
                        isLoading = isLoading,
                        selectedInstitution = selectedInstitution,
                        selectedDate = selectedDate,
                        selectedSchedule = selectedSchedule,
                        patientNotes = patientNotes,
                        currentMonth = currentMonth,
                        showConfirmDialog = showConfirmDialog,
                        doctorName = doctorName,
                        onInstitutionSelected = {
                            selectedInstitution = it; selectedDate = null; selectedSchedule = null
                        },
                        onDateSelected = { selectedDate = it; selectedSchedule = null },
                        onScheduleSelected = { selectedSchedule = it },
                        onNotesChanged = { patientNotes = it },
                        onMonthChanged = { currentMonth = it },
                        onShowDialog = { showConfirmDialog = it },
                        onConfirmBooking = {
                            viewModel.bookAppointment(
                                selectedSchedule!!.id,
                                patientNotes.ifEmpty { null })
                            showConfirmDialog = false
                        }
                    )
                }
            }
        }
    }
}