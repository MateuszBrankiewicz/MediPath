package com.medipath.core.services

import android.util.Log
import com.google.gson.Gson
import com.medipath.core.models.NotificationMessage
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.Observable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage

class UserNotificationsService(private val authToken: String? = null) {

    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()
    private val gson = Gson()
    @Volatile
    private var isConnecting = false

    private val _notifications = PublishSubject.create<NotificationMessage>()
    val notifications: Observable<NotificationMessage> = _notifications

    fun connect(baseUrl: String) {
        if (isConnecting || stompClient?.isConnected == true) {
            return
        }

        isConnecting = true
        val wsUrl = baseUrl.replace("https", "ws").replace("http", "ws") + "/ws/websocket"

        try {
            val headers = mutableMapOf<String, String>()
            authToken?.let {
                headers["Cookie"] = "SESSION=$it"
            }
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl, headers).apply {
                withClientHeartbeat(10000).withServerHeartbeat(10000)
            }

            val lifecycleDisposable: Disposable = stompClient!!.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ lifecycleEvent ->
                    when (lifecycleEvent.type) {
                        LifecycleEvent.Type.OPENED -> {
                            isConnecting = false
                            subscribeToNotifications()
                        }
                        LifecycleEvent.Type.ERROR -> {
                            isConnecting = false
                        }
                        LifecycleEvent.Type.CLOSED -> {
                            isConnecting = false
                        }
                        LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                        }
                    }
                }, { throwable ->
                    isConnecting = false
                })

            compositeDisposable.add(lifecycleDisposable)

            stompClient!!.connect()
        } catch (_: Exception) {
            isConnecting = false
        }
    }

    private fun subscribeToNotifications() {
        stompClient?.let { client ->
            try {
                val notificationsDisposable: Disposable = client.topic("/user/notifications")
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({ stompMessage: StompMessage ->
                        try {
                            val notification = gson.fromJson(stompMessage.payload, NotificationMessage::class.java)
                            _notifications.onNext(notification)
                        } catch (e: Exception) {
                            Log.e("UserNotificationsService", "Error parsing notification: ${e.message}", e)
                        }
                    }, { throwable ->
                        Log.e("UserNotificationsService", "Error subscribing to notifications: ${throwable.message}", throwable)
                    })

                compositeDisposable.add(notificationsDisposable)
            } catch (e: Exception) {
                Log.e("UserNotificationsService", "Error subscribing to notifications", e)
            }
        }
    }

    fun disconnect() {
        try {
            stompClient?.let { client ->
                if (client.isConnected) {
                    client.disconnect()
                }
            }
            compositeDisposable.clear()
            stompClient = null
            isConnecting = false
        } catch (e: Exception) {
            Log.e("UserNotificationsService", "Error during disconnect", e)
        }
    }
}