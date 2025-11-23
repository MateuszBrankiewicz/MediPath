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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.models.VisitDetails
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.shared.components.getTranslatedStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.medipath.R

@Composable
fun VisitDetailsContent(
    visitDetails: VisitDetails,
    onReviewClick: () -> Unit,
    onSeeReviewClick: () -> Unit
) {
    val colors = LocalCustomColors.current
    val scrollState = rememberScrollState()
    val hasReview = !visitDetails.commentId.isNullOrEmpty()

    val startDateTime = try {
        val parsedDateTime = LocalDateTime.parse(
            visitDetails.time.startTime,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        parsedDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
    } catch (_: Exception) {
        visitDetails.time.startTime
    }

    val endDateTime = try {
        val parsedDateTime = LocalDateTime.parse(
            visitDetails.time.endTime,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )
        parsedDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
    } catch (_: Exception) {
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
                        text = stringResource(R.string.status_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = getTranslatedStatus(visitDetails.status),
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

            InfoCard(title = stringResource(R.string.doctor)) {
                InfoRow(stringResource(R.string.name), "Dr. ${visitDetails.doctor.doctorName} ${visitDetails.doctor.doctorSurname}")
                InfoRow(stringResource(R.string.specialisations), visitDetails.doctor.specialisations.joinToString(", "))
            }

            InfoCard(title = stringResource(R.string.patient)) {
                InfoRow(stringResource(R.string.name), "${visitDetails.patient.name} ${visitDetails.patient.surname}")
                InfoRow(stringResource(R.string.government_id), visitDetails.patient.govID)
            }

            InfoCard(title = stringResource(R.string.visit_time)) {
                InfoRow(stringResource(R.string.start), startDateTime)
                InfoRow(stringResource(R.string.end), endDateTime)
            }

            InfoCard(title = stringResource(R.string.institution)) {
                InfoRow(stringResource(R.string.name), visitDetails.institution.institutionName)
            }

            if (visitDetails.note.isNotEmpty()) {
                InfoCard(title = stringResource(R.string.doctor_notes)) {
                    Text(
                        text = visitDetails.note,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (visitDetails.patientRemarks.isNotEmpty()) {
                InfoCard(title = stringResource(R.string.patient_remarks)) {
                    Text(
                        text = visitDetails.patientRemarks,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            val prescriptions = visitDetails.codes.filter { it.codeType == "PRESCRIPTION" }
            if (prescriptions.isNotEmpty()) {
                CodesCard(title = stringResource(R.string.prescriptions), codes = prescriptions, color = colors.blue800)
            }

            val referrals = visitDetails.codes.filter { it.codeType == "REFERRAL" }
            if (referrals.isNotEmpty()) {
                CodesCard(title = stringResource(R.string.referrals), codes = referrals, color = colors.orange800)
            }
        }



        if (visitDetails.status.lowercase() == "completed") {
            Button(
                onClick = if (hasReview) onSeeReviewClick else onReviewClick,
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
                    text = if (hasReview) stringResource(R.string.see_review) else stringResource(R.string.add_review),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}