package com.medipath.modules.patient.booking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.models.Comment
import com.medipath.core.theme.LocalCustomColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CommentCard(
    comment: Comment,
    isDoctorContext: Boolean = false
) {
    val colors = LocalCustomColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = comment.author,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isDoctorContext) {
                        Text(
                            text = "dla: ${comment.institution}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        Text(
                            text = "dla: ${comment.doctor}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    if (comment.doctorRating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Doctor Rating",
                                tint = colors.blue900,
                                modifier = Modifier.size(16.dp)
                            )
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = colors.yellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = String.format("%.1f", comment.doctorRating),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (comment.institutionRating > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Business,
                                contentDescription = "Institution Rating",
                                tint = colors.blue900,
                                modifier = Modifier.size(16.dp)
                            )
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = colors.yellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = String.format("%.1f", comment.institutionRating),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    val formattedDate = remember(comment.createdAt) {
                        try {
                            val dateTime = LocalDateTime.parse(comment.createdAt)
                            dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                        } catch (e: java.time.format.DateTimeParseException) {
                            comment.createdAt.split("T").firstOrNull() ?: comment.createdAt
                        } catch (e: Exception) {
                            comment.createdAt
                        }
                    }

                    formattedDate?.let { date ->
                        Text(
                            text = date,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            if (comment.content.isNotEmpty()) {
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
    }
}