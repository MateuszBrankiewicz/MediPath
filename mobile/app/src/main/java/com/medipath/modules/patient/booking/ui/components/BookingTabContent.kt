package com.medipath.modules.patient.booking.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.core.models.DoctorScheduleItem
import com.medipath.core.models.Institution
import com.medipath.core.utils.LocaleHelper
import com.medipath.modules.shared.components.CalendarCard
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.collections.chunked

@Composable
fun BookingTabContent(
    schedules: List<DoctorScheduleItem>,
    isLoading: Boolean,
    selectedInstitution: Institution?,
    selectedDate: LocalDate?,
    selectedSchedule: DoctorScheduleItem?,
    patientNotes: String,
    currentMonth: LocalDate,
    showConfirmDialog: Boolean,
    doctorName: String,
    onInstitutionSelected: (Institution?) -> Unit,
    onDateSelected: (LocalDate?) -> Unit,
    onScheduleSelected: (DoctorScheduleItem?) -> Unit,
    onNotesChanged: (String) -> Unit,
    onMonthChanged: (LocalDate) -> Unit,
    onShowDialog: (Boolean) -> Unit,
    onConfirmBooking: () -> Unit
) {
    val institutions = remember(schedules) {
        schedules.map { it.institution }.distinctBy { it.institutionId }
    }

    val filteredSchedules = remember(schedules, selectedInstitution) {
        if (selectedInstitution == null) schedules
        else schedules.filter { it.institution.institutionId == selectedInstitution.institutionId }
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

    if (showConfirmDialog && selectedSchedule != null) {
        val context = LocalContext.current
        val locale = LocaleHelper.getLocale(context)
        
        AlertDialog(
            onDismissRequest = { onShowDialog(false) },
            title = { Text(stringResource(R.string.confirm_appointment)) },
            text = {
                Column {
                    val dateTime = LocalDateTime.parse(selectedSchedule.startHour, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                    Text(stringResource(R.string.doctor_label, doctorName))
                    Text(
                        stringResource(
                            R.string.institution_label,
                            selectedSchedule.institution.institutionName
                        ))
                    Text(
                        stringResource(
                            R.string.date_label,
                            dateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", locale))
                        ))
                    Text(
                        stringResource(
                            R.string.time_label,
                            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                        ))
                    if (patientNotes.isNotEmpty()) {
                        Text(stringResource(R.string.notes_label, patientNotes))
                    }
                }
            },
            confirmButton = {
                Button(onClick = onConfirmBooking) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onShowDialog(false) }) {
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
        val context = LocalContext.current
        val locale = LocaleHelper.getLocale(context)
        
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
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (institutions.size > 1) {
                        item {
                            Text(
                                text = stringResource(R.string.select_institution),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp)
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
                                            onInstitutionSelected(institution)
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
                            text = stringResource(R.string.select_date),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    item {
                        CalendarCard(
                            currentMonth = currentMonth,
                            availableDates = availableDates,
                            dateCountMap = dateCountMap,
                            selectedDate = selectedDate,
                            onMonthChange = onMonthChanged,
                            onDateSelected = onDateSelected
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
                                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM, yyyy", locale)),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        items(schedulesForSelectedDate.chunked(3)) { scheduleRow ->
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
                                                onScheduleSelected(schedule)
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

                    } else if (selectedDate != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.no_available_appointments_for_this_date),
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    if (selectedSchedule != null) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(R.string.patient_notes_optional),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = patientNotes,
                                onValueChange = onNotesChanged,
                                placeholder = { Text(stringResource(R.string.add_any_notes_for_the_doctor)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                        }

                        item {
                            Button(
                                onClick = { onShowDialog(true) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(30.dp),
                                enabled = !isLoading
                            ) {
                                Text(stringResource(R.string.book_appointment), fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
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