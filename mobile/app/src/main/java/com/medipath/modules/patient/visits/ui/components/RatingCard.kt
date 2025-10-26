package com.medipath.modules.patient.visits.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round

@Composable
fun RatingCard(
    title: String,
    subtitle: String,
    rating: Double,
    onRatingChange: (Double) -> Unit,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    val fullStar = index + 1.0
                    val halfStar = index + 0.5

                    Icon(
                        imageVector = when {
                            rating >= fullStar -> Icons.Default.Star
                            rating >= halfStar -> Icons.AutoMirrored.Filled.StarHalf
                            else -> Icons.Default.StarBorder
                        },
                        contentDescription = "Star ${index + 1}",
                        tint = if (rating >= halfStar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = rating.toFloat(),
                    onValueChange = {
                        val roundedValue = (round(it * 2) / 2.0)
                        onRatingChange(roundedValue.coerceIn(0.5, 5.0))
                    },
                    valueRange = 0.5f..5f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = color,
                        activeTrackColor = color,
                        inactiveTrackColor = color.copy(alpha = 0.3f)
                    )
                )

                if (rating > 0) {
                    Text(
                        text = when {
                            rating <= 1.0 -> "Very poor"
                            rating <= 2.0 -> "Poor"
                            rating <= 3.0 -> "Fair"
                            rating <= 4.0 -> "Good"
                            else -> "Excellent"
                        } + " (${"%.1f".format(rating)}/5.0)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = color,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}