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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.models.DoctorScheduleItem
import com.medipath.core.models.Institution
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

    val availableDates = remember(schedulesByDate) {
        schedulesByDate.keys.toSet()
    }

    val schedulesForSelectedDate = remember(selectedDate, schedulesByDate) {
        selectedDate?.let { schedulesByDate[it] } ?: emptyList()
    }

    if (showConfirmDialog && selectedSchedule != null) {
        AlertDialog(
            onDismissRequest = { onShowDialog(false) },
            title = { Text("Confirm Appointment") },
            text = {
                Column {
                    val dateTime = LocalDateTime.parse(selectedSchedule.startHour, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                    Text("Doctor: $doctorName")
                    Text("Institution: ${selectedSchedule.institution.institutionName}")
                    Text("Date: ${dateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))}")
                    Text("Time: ${dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                    if (patientNotes.isNotEmpty()) {
                        Text("Notes: $patientNotes")
                    }
                }
            },
            confirmButton = {
                Button(onClick = onConfirmBooking) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { onShowDialog(false) }) {
                    Text("Cancel")
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
                            text = "No available appointments",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Please check back later",
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
                                text = "Select Institution",
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
                            text = "Select Date",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    item {
                        CalendarView(
                            currentMonth = currentMonth,
                            availableDates = availableDates,
                            selectedDate = selectedDate,
                            onMonthChange = onMonthChanged,
                            onDateSelected = onDateSelected
                        )
                    }

                    if (selectedDate != null && schedulesForSelectedDate.isNotEmpty()) {
                        item {
                            Text(
                                text = "Available Times",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }

                        item {
                            Text(
                                text = selectedDate!!.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
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
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = "No available appointments for this date",
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    if (selectedSchedule != null) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Patient Notes (Optional)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = patientNotes,
                                onValueChange = onNotesChanged,
                                placeholder = { Text("Add any notes for the doctor...") },
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
                                Text("BOOK APPOINTMENT", fontSize = 16.sp, modifier = Modifier.padding(vertical = 8.dp))
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