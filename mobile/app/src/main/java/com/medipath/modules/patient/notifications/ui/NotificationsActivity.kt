package com.medipath.modules.patient.notifications.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medipath.core.theme.MediPathTheme
import com.medipath.core.theme.LocalCustomColors
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.modules.patient.notifications.NotificationsViewModel
import com.medipath.core.models.Notification

class NotificationsActivity : ComponentActivity() {
    private lateinit var sessionManager: DataStoreSessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = DataStoreSessionManager(this)

        setContent {
            MediPathTheme {
                NotificationsScreen(
                    onBackClick = { finish() },
                    sessionManager = sessionManager
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val vm = androidx.lifecycle.ViewModelProvider(this).get(NotificationsViewModel::class.java)
            vm.fetchNotifications(sessionManager)
        } catch (e: Exception) {
            android.util.Log.e("NotificationsActivity", "Error fetching notifications onResume", e)
        }
    }
}

@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    sessionManager: DataStoreSessionManager
) {
    val colors = LocalCustomColors.current
    val viewModel: NotificationsViewModel = viewModel()
    val notifications by viewModel.notifications
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val context = LocalContext.current

    val detailsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.fetchNotifications(sessionManager)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalCustomColors.current.blue900)
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Return",
                    tint = MaterialTheme.colorScheme.background
                )
            }
            Text(
                text = "Notifications",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(start = 8.dp).padding(vertical = 24.dp)
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            error.isNotEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error,
                        color = colors.red800,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchNotifications(sessionManager) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Try again")
                    }
                }
            }
            notifications.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                        .background(MaterialTheme.colorScheme.secondary),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No notifications yet",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            else -> {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.markAllAsRead(sessionManager) },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text(
                                text = "Mark all as read",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        itemsIndexed(notifications) { index, notification ->
                            NotificationItem(notification = notification) {
                                val intent = Intent(context, NotificationDetailsActivity::class.java).apply {
                                    putExtra("notification", notification)
                                    putExtra("notification_index", index)
                                }
                                detailsLauncher.launch(intent)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    val colors = LocalCustomColors.current
    val timestamp = notification.timestamp
    val dateTime = "${String.format("%02d", timestamp[2])}.${String.format("%02d", timestamp[1])}.${timestamp[0]} ${String.format("%02d", timestamp[3])}:${String.format("%02d", timestamp[4])}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.read)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (notification.system)
                    Icons.Default.NotificationsActive
                else
                    Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (notification.system) colors.blue800 else colors.orange800
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!notification.read) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(5.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = notification.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = dateTime,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}