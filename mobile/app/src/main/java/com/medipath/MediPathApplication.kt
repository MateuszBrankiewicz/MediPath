package com.medipath

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.medipath.core.network.RetrofitInstance
import com.medipath.core.services.UserNotificationsService
import com.medipath.modules.shared.notifications.ui.NotificationsActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MediPathApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var notificationsService: UserNotificationsService? = null
    private val disposable = CompositeDisposable()
    private val channelId = "medipath_notifications"
    private var notificationId = 1

    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.initialize(applicationContext)
        createNotificationChannel()
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun shouldRequestNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false
        }
        return !hasNotificationPermission()
    }

    fun initializeWebSocket() {
        val sessionManager = RetrofitInstance.getSessionManager()
        val token = sessionManager.getSessionId()

        if (token == null) {
            return
        }

        if (notificationsService != null) {
            return
        }

        applicationScope.launch(Dispatchers.IO) {
            try {
                notificationsService = UserNotificationsService(token)
                
                val subscription = notificationsService!!.notifications
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ notification ->
                        showNotification(notification.title, notification.content, notification.timestamp)
                    })
                
                disposable.add(subscription)
                notificationsService!!.connect(RetrofitInstance.getWebsocketUrl())
            } catch (e: Exception) {
                Log.e("MediPathApp", "Error initializing WebSocket", e)
                notificationsService = null
            }
        }
    }

    fun reconnectWebSocketIfNeeded() {
        if (notificationsService == null) {
            initializeWebSocket()
        }
    }

    fun disconnectWebSocket() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                notificationsService?.disconnect()
                notificationsService = null
                disposable.clear()
            } catch (e: Exception) {
                Log.e("MediPathApp", "Error disconnecting WebSocket", e)
            }
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.medipath_notifications)
        val descriptionText = getString(R.string.notifications_from_medipath)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, content: String, timestamp: String?) {
        if (!hasNotificationPermission()) {
            Log.w("MediPath", "Cannot show notification - permission not granted")
            return
        }
        
        val intent = Intent(this, NotificationsActivity::class.java)
        val requestCode = (title + (timestamp ?: "")).hashCode()
        val pendingIntent = PendingIntent.getActivity(
            this, requestCode, intent,
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
                }
            }
        } catch (e: Exception) {
            Log.e("MediPath", "Error showing notification", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        disconnectWebSocket()
    }
}