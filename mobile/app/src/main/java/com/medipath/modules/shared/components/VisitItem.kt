package com.medipath.modules.shared.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.R
import com.medipath.core.models.Visit
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.utils.LocaleHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun VisitItem(
    visit: Visit,
    onCancelVisit: (String) -> Unit,
    onViewDetails: ((String) -> Unit)? = null,
    onReschedule: ((String) -> Unit)? = null,
    elevation: CardElevation = CardDefaults.cardElevation()
) {
    val context = LocalContext.current
    val locale = LocaleHelper.getLocale(context)
    val colors = LocalCustomColors.current

    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val outputDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", locale)
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

    val dateFormatted = startDateTime?.format(outputDateFormatter) ?: stringResource(R.string.unknown_date)
    val timeFormatted = startDateTime?.format(outputTimeFormatter) ?: "--:--"

    var showCancelDialog by remember { mutableStateOf(false) }

    val rawStatus = visit.status.lowercase()

    val isScheduled = rawStatus == "upcoming" || rawStatus == "scheduled"
    val isCancelled = rawStatus == "cancelled"
    val isCompleted = rawStatus == "completed"

    val canReschedule = isScheduled
    val canViewDetails = isCompleted

    val (statusText, statusColor) = when {
        isScheduled -> stringResource(R.string.upcoming) to colors.orange800
        isCancelled -> stringResource(R.string.cancelled) to colors.red800
        isCompleted -> stringResource(R.string.completed) to colors.green800
        else -> visit.status to MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        elevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dr. ${visit.doctor.doctorName} ${visit.doctor.doctorSurname}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = visit.doctor.specialisations.joinToString(", "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = statusText,
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
            
            Spacer(modifier = Modifier.height(8.dp))

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
            
            if (visit.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.note_title, visit.note),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onViewDetails?.invoke(visit.id) },
                    enabled = canViewDetails && onViewDetails != null,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.blue800,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(stringResource(R.string.details), fontSize = 12.sp)
                }
                Button(
                    onClick = { onReschedule?.invoke(visit.id) },
                    enabled = canReschedule && onReschedule != null,
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.purple800,
                        contentColor = MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(stringResource(R.string.reschedule_capitals), fontSize = 12.sp)
                }
                if (isScheduled) {
                    Button(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.red800,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Text(stringResource(R.string.cancel_capitals), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.confirm_cancellation)) },
            text = { Text(
                stringResource(
                    R.string.confirm_cancellation_visit,
                    visit.doctor.doctorName,
                    visit.doctor.doctorSurname
                )) },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelVisit(visit.id)
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}