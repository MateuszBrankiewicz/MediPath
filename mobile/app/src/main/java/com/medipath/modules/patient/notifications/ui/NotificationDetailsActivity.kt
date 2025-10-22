package com.medipath.modules.patient.notifications.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.theme.MediPathTheme

class NotificationDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notification = intent.getSerializableExtra("notification") as? com.medipath.core.models.Notification
        val notificationIndex = intent.getIntExtra("notification_index", -1)

        setContent {
            MediPathTheme {
                if (notification != null) {
                    NotificationDetailsScreen(
                        notification = notification,
                        onDelete = {
                            if (notificationIndex != -1) {
                                val data = intent
                                data.putExtra("deleted_notification_index", notificationIndex)
                                setResult(Activity.RESULT_OK, data)
                                finish()
                            } else {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        },
                        onClose = { finish() }
                    )
                } else {
                    Text("Notification details unavailable")
                }
            }
        }

    }
}

@Composable
fun NotificationDetailsScreen(
    notification: com.medipath.core.models.Notification,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.navigationBars.asPaddingValues())
                .padding( 20.dp)
        ) {
            Text(
                text = notification.title,
                fontSize = 26.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = customColors.blue900
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                val ts = notification.timestamp
                val dateStr = if (ts.size >= 5) "%02d.%02d.%04d %02d:%02d".format(ts[2], ts[1], ts[0], ts[3], ts[4])
                              else if (ts.size >= 3) "%02d.%02d.%04d".format(ts[2], ts[1], ts[0]) else ""
                Text(
                    text = dateStr,
                    fontSize = 14.sp,
                    color = colors.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                if (notification.system) {
                    AssistChip(
                        onClick = {},
                        label = { Text("System") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = colors.secondaryContainer)
                    )
                }
                if (!notification.read) {
                    AssistChip(
                        onClick = {},
                        label = { Text("New") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = colors.primaryContainer)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = notification.content,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        color = colors.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, customColors.blue900),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = customColors.blue900)
                ) {
                    Text(text = "Close")
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(text = "Delete", color = colors.onError)
                }
            }
        }
    }
}