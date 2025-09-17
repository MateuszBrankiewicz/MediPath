package com.medipath.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.data.models.Visit
import com.medipath.ui.theme.LocalCustomColors

@Composable
fun VisitItem(visit: Visit) {
    val colors = LocalCustomColors.current
    val startTime = visit.time.startTime
    val dateTime = "${String.format("%02d", startTime[2])}.${String.format("%02d", startTime[1])}.${startTime[0]} ${String.format("%02d", startTime[3])}:${String.format("%02d", startTime[4])}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dr. ${visit.doctor.doctorName} ${visit.doctor.doctorSurname}",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = visit.doctor.specialisations.joinToString(", "),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = dateTime,
                fontSize = 14.sp
            )
            Text(
                text = visit.institution.institutionName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.red800,
                contentColor = MaterialTheme.colorScheme.background
            )
        ) {
            Text("CANCEL")
        }
    }
}