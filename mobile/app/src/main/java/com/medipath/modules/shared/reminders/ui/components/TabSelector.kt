package com.medipath.modules.shared.reminders.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.theme.LocalCustomColors

@Composable
fun TabSelector(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val colors = LocalCustomColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { onTabSelected("Received") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == "Received") colors.blue900 else MaterialTheme.colorScheme.surface,
                contentColor = if (selectedTab == "Received") MaterialTheme.colorScheme.background else colors.blue900
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (selectedTab == "Received") 4.dp else 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "RECEIVED",
                fontSize = 12.sp,
                fontWeight = if (selectedTab == "Received") FontWeight.Bold else FontWeight.Normal
            )
        }

        Button(
            onClick = { onTabSelected("Scheduled") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == "Scheduled") colors.blue900 else MaterialTheme.colorScheme.surface,
                contentColor = if (selectedTab == "Scheduled") MaterialTheme.colorScheme.background else colors.blue900
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (selectedTab == "Scheduled") 4.dp else 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SCHEDULED",
                fontSize = 12.sp,
                fontWeight = if (selectedTab == "Scheduled") FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}