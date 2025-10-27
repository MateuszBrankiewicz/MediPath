package com.medipath.modules.patient.visits.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.theme.LocalCustomColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun VisitDetailsContent(
    visitDetails: com.medipath.core.models.VisitDetails,
    onReviewClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val scrollState = rememberScrollState()

    val startDateTime = try {
        val parsedDateTime = LocalDateTime.parse(
            visitDetails.time.startTime,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        parsedDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
    } catch (e: Exception) {
        visitDetails.time.startTime
    }

    val endDateTime = try {
        val parsedDateTime = LocalDateTime.parse(
            visitDetails.time.endTime,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        parsedDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
    } catch (e: Exception) {
        visitDetails.time.endTime
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val statusColor = when (visitDetails.status.lowercase()) {
                "upcoming" -> colors.orange800
                "cancelled" -> colors.red800
                "completed" -> colors.green800
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = visitDetails.status,
                        fontSize = 14.sp,
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
            }

            InfoCard(title = "Doctor") {
                InfoRow("Name", "Dr. ${visitDetails.doctor.doctorName} ${visitDetails.doctor.doctorSurname}")
                InfoRow("Specializations", visitDetails.doctor.specialisations.joinToString(", "))
            }

            InfoCard(title = "Patient") {
                InfoRow("Name", "${visitDetails.patient.name} ${visitDetails.patient.surname}")
                InfoRow("Government ID", visitDetails.patient.govID)
            }

            InfoCard(title = "Visit Time") {
                InfoRow("Start", startDateTime)
                InfoRow("End", endDateTime)
            }

            InfoCard(title = "Institution") {
                InfoRow("Name", visitDetails.institution.institutionName)
            }

            if (visitDetails.note.isNotEmpty()) {
                InfoCard(title = "Doctor's Note") {
                    Text(
                        text = visitDetails.note,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (visitDetails.patientRemarks.isNotEmpty()) {
                InfoCard(title = "Patient Remarks") {
                    Text(
                        text = visitDetails.patientRemarks,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            val prescriptions = visitDetails.codes.filter { it.codeType == "PRESCRIPTION" }
            if (prescriptions.isNotEmpty()) {
                CodesCard(title = "Prescriptions", codes = prescriptions, color = colors.blue800)
            }

            val referrals = visitDetails.codes.filter { it.codeType == "REFERRAL" }
            if (referrals.isNotEmpty()) {
                CodesCard(title = "Referrals", codes = referrals, color = colors.orange800)
            }
        }

        if (visitDetails.status.lowercase() == "completed") {
            Button(
                onClick = onReviewClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.blue900
                ),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = "REVIEW VISIT",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}