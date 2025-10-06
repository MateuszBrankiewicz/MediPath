package com.medipath.modules.patient.home.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.medipath.core.theme.MediPathTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.MedicalInformation
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.UserNotificationsService
import com.medipath.modules.shared.auth.ui.LoginActivity
import com.medipath.modules.patient.codes.ui.CodesActivity
import com.medipath.modules.shared.components.InfoCard
import com.medipath.modules.shared.components.MenuCard
import com.medipath.modules.shared.components.Navigation
import com.medipath.modules.shared.components.SearchBar
import com.medipath.modules.shared.components.VisitItem
import com.medipath.modules.patient.notifications.ui.NotificationsActivity
import com.medipath.core.theme.LocalCustomColors
import com.medipath.modules.patient.home.HomeViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeActivity : ComponentActivity() {

    private lateinit var notificationsService: UserNotificationsService
    private val activityDisposable = CompositeDisposable()
    private val channelId = "medipath_notifications"
    private var notificationId = 1
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("HomeActivity", "Uprawnienia do powiadomień udzielone")
        } else {
            Log.w("HomeActivity", "Uprawnienia do powiadomień odrzucone")
            Toast.makeText(this, "Bez uprawnień do powiadomień nie będziesz otrzymywać alertów", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        Log.d("HomeActivity", "onCreate started - launching WebSocket setup")
        
        createNotificationChannel()
        checkNotificationPermission()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sessionManager = DataStoreSessionManager(this@HomeActivity)
                val token = sessionManager.getSessionId()
                Log.d("HomeActivity", "Using token for WebSocket: $token")
                notificationsService = UserNotificationsService(token)

                val notificationSubscription = notificationsService.notifications
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { notification ->
                        Log.d("HomeActivity", "Received notification: ${notification.title} - ${notification.content}")
                        showNotification(notification.title, notification.content)
                    }
                activityDisposable.add(notificationSubscription)
                notificationsService.connect("http://10.0.2.2:8080/ws")
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error initializing notifications service", e)
            }
        }

        enableEdgeToEdge()
        val sessionManager = DataStoreSessionManager(this)
        val authService = RetrofitInstance.authService
        
        setContent {
            MediPathTheme {
                HomeScreen(
                    onLogoutClick = {
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                authService.logout()
                            } catch (e: Exception) {
                                Log.e("HomeActivity", "Logout error", e)
                            } finally {
                                withContext(Dispatchers.Main) {
                                    sessionManager.deleteSessionId()
                                    startActivity(Intent(this@HomeActivity, LoginActivity::class.java))
                                    finish()
                                }
                            }
                        }
                    },
                    sessionManager = sessionManager
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MediPath Powiadomienia"
            val descriptionText = "Powiadomienia z aplikacji MediPath"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("HomeActivity", "Uprawnienia do powiadomień już udzielone")
                }
                else -> {
                    Log.d("HomeActivity", "Żądanie uprawnień do powiadomień")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("HomeActivity", "Android < 13, uprawnienia do powiadomień nie wymagane")
        }
    }

    private fun showNotification(title: String, content: String) {
        Log.d("HomeActivity", "Próba wyświetlenia powiadomienia: $title")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.w("HomeActivity", "Brak uprawnień do powiadomień - fallback do Toast")
                Toast.makeText(this, "Nowe powiadomienie: $title", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        val intent = Intent(this, NotificationsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(this)) {
                if (areNotificationsEnabled()) {
                    notify(notificationId++, builder.build())
                    Log.d("HomeActivity", "Powiadomienie wyświetlone pomyślnie: $title")
                } else {
                    Log.w("HomeActivity", "Powiadomienia wyłączone przez użytkownika")
                    Toast.makeText(this@HomeActivity, "Nowe powiadomienie: $title", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Log.w("HomeActivity", "Błąd SecurityException przy wyświetlaniu powiadomienia", e)
            Toast.makeText(this, "Nowe powiadomienie: $title", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("HomeActivity", "Błąd przy wyświetlaniu powiadomienia", e)
            Toast.makeText(this, "Nowe powiadomienie: $title", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (::notificationsService.isInitialized) {
                    notificationsService.disconnect()
                }
                activityDisposable.clear()
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error during cleanup", e)
            }
        }
        Log.d("HomeActivity", "Activity destroyed, WebSocket disconnected.")
    }
}

@Composable
fun HomeScreen(
    onLogoutClick: () -> Unit = {},
    viewModel: HomeViewModel = remember { HomeViewModel() },
    sessionManager: DataStoreSessionManager
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile(sessionManager)
    }

    val shouldRedirectToLogin by viewModel.shouldRedirectToLogin
    LaunchedEffect(shouldRedirectToLogin) {
        if (shouldRedirectToLogin) {
            Log.w("HomeScreen", "Redirecting to login due to expired session")
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            (context as? ComponentActivity)?.finish()
        }
    }

    val firstName by viewModel.firstName
    val upcomingVisits by viewModel.upcomingVisits
    val deleteSuccess by viewModel.deleteSuccess
    val deleteError by viewModel.deleteError
    val prescriptionCode by viewModel.prescriptionCode
    val referralCode by viewModel.referralCode

    Navigation(
        onNotificationsClick = {
            context.startActivity(Intent(context, NotificationsActivity::class.java))
        },
        content = { innerPadding ->
            val colors = LocalCustomColors.current
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                item {
                    SearchBar()
                }
                
                item {
                    Column(
                        modifier = Modifier
                            .padding(vertical = 20.dp, horizontal = 30.dp)
                    ) {
                        Text(
                            text = "Dashboard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        InfoCard(
                            title = "Prescriptions",
                            label = "Code:",
                            code = prescriptionCode.ifEmpty { "No active prescriptions" },
                            onClick = {
                                val intent = Intent(context, CodesActivity::class.java)
                                intent.putExtra("code_type", "PRESCRIPTION")
                                intent.putExtra("user_id", viewModel.getCurrentUserId())
                                context.startActivity(intent)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        InfoCard(
                            title = "Referrals",
                            label = "Code:",
                            code = referralCode.ifEmpty { "No active referrals" },
                            onClick = {
                                val intent = Intent(context, CodesActivity::class.java)
                                intent.putExtra("code_type", "REFERRAL")
                                intent.putExtra("user_id", viewModel.getCurrentUserId())
                                context.startActivity(intent)
                            }
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MenuCard(
                                icon = Icons.AutoMirrored.Outlined.List,
                                title = "Visits",
                                onClick = { println("Visits clicked!") },
                                backgroundColor = colors.purple800,
                                iconColor = colors.purple300
                            )

                            MenuCard(
                                icon = Icons.Outlined.MedicalInformation,
                                title = "Medical History",
                                onClick = { println("History clicked!") },
                                backgroundColor = colors.blue800,
                                iconColor = colors.blue300
                            )

                            MenuCard(
                                icon = Icons.AutoMirrored.Outlined.Comment,
                                title = "Opinions",
                                onClick = { println("Opinions clicked!") },
                                backgroundColor = colors.orange800,
                                iconColor = colors.orange300
                            )

                            MenuCard(
                                icon = Icons.Outlined.Notifications,
                                title = "Reminders",
                                onClick = { println("Reminders clicked!") },
                                backgroundColor = colors.green800,
                                iconColor = colors.green300
                            )
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.background,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        ) {
                            Text(
                                "Upcoming visits",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Visit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        HorizontalDivider(thickness = 2.dp)
                    }
                }
                
                if (upcomingVisits.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                "No upcoming visits",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                } else {
                    items(upcomingVisits) { visit ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 20.dp)
                        ) {
                            VisitItem(
                                visit = visit,
                                onCancelVisit = { visitId ->
                                    viewModel.cancelVisit(visitId, sessionManager)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        },
        onLogoutClick = onLogoutClick,
        firstName = firstName
    )

    if (deleteSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteMessages() },
            title = { Text("Sukces") },
            text = { Text("Wizyta została pomyślnie anulowana") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearDeleteMessages() }) {
                    Text("OK")
                }
            }
        )
    }

    if (deleteError.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDeleteMessages() },
            title = { Text("Błąd") },
            text = { Text(deleteError) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearDeleteMessages() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
//    MediPathTheme { HomeScreen() }
}