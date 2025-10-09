package com.medipath.modules.patient.booking.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.theme.MediPathTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AppointmentBookingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val doctorName = intent.getStringExtra("doctor_name") ?: ""

        setContent {
            MediPathTheme {
                AppointmentBookingScreen(
                    doctorName = doctorName,
                    onBackClick = { finish() },
                    onConfirm = {  }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentBookingScreen(
    doctorName: String,
    onBackClick: () -> Unit,
    onConfirm: () -> Unit
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var selectedInstitution by remember { mutableStateOf("") }
    var patientNotes by remember { mutableStateOf("") }

    val availableDates = remember { mutableListOf<LocalDate>() }

    val availableTimes = remember { mutableListOf<LocalTime>() }

    val institutions = listOf("Klinia GPS Zalas", "Medical Center", "Health Plus")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Book Appointment",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Doctor: $doctorName",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Select Institution",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        items(institutions) { institution ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clickable { selectedInstitution = institution }
                    .border(
                        width = if (selectedInstitution == institution) 2.dp else 0.dp,
                        color = if (selectedInstitution == institution) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Text(
                    text = institution,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Select Date",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableDates) { date ->
                    Card(
                        modifier = Modifier
                            .clickable { selectedDate = date }
                            .border(
                                width = if (selectedDate == date) 2.dp else 0.dp,
                                color = if (selectedDate == date) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("MMM")),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = date.dayOfMonth.toString(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("EEE")),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedDate != null) {
                Text(
                    text = "Select Time",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (selectedDate != null) {
            items(availableTimes.chunked(4)) { timeRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    timeRow.forEach { time ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTime = time }
                                .border(
                                    width = if (selectedTime == time) 2.dp else 0.dp,
                                    color = if (selectedTime == time) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                        ) {
                            Text(
                                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        item {
            if (selectedTime != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Patient Notes (Optional)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = patientNotes,
                    onValueChange = { patientNotes = it },
                    placeholder = { Text("Add any notes for the doctor...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = selectedDate != null && selectedTime != null && selectedInstitution.isNotEmpty()
                    ) {
                        Text("CONFIRM")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}