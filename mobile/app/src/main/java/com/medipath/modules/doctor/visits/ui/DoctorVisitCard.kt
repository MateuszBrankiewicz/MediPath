package com.medipath.modules.doctor.visits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.models.Visit
import com.medipath.core.theme.LocalCustomColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DoctorVisitCard(
    visit: Visit,
    onViewDetails: () -> Unit,
    onCancel: (String) -> Unit
) {
    val colors = LocalCustomColors.current
    var showCancelDialog by remember { mutableStateOf(false) }

    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val outputDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    val outputTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    val startDateTime = try {
        LocalDateTime.parse(visit.time.startTime, inputFormatter)
    } catch (_: Exception) {
        try {
            LocalDateTime.parse(visit.time.startTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: Exception) {
            null
        }
    }

    val dateFormatted = startDateTime?.format(outputDateFormatter) ?: "Invalid Date"
    val timeFormatted = startDateTime?.format(outputTimeFormatter) ?: "--:--"

    val isUpcoming = visit.status == "Upcoming"
    
    val statusColor = when (visit.status) {
        "Upcoming" -> colors.orange800
        "Completed" -> colors.green800
        "Cancelled" -> colors.red800
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${visit.patient.name} ${visit.patient.surname}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "GovID: ${visit.patient.govID}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = visit.status,
                    fontSize = 12.sp,
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = dateFormatted,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    if (timeFormatted.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = timeFormatted,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = visit.institution.institutionName,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onViewDetails,
                    enabled = visit.status == "Completed",
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.purple800,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text("DETAILS", fontSize = 12.sp)
                }

                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.blue800,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text("PATIENT", fontSize = 12.sp)
                }
                
                if (isUpcoming) {
                    Button(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.red800,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Text("CANCEL", fontSize = 12.sp)
                    }
                }
            }
        }
    }
    
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Confirm Cancellation") },
            text = { Text("Are you sure you want to cancel this visit with ${visit.patient.name} ${visit.patient.surname}?") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancel(visit.id)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}
