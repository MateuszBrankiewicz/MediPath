package com.medipath.modules.shared.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.medipath.core.models.Notification
import com.medipath.core.services.NotificationsService
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationsViewModel(
    private val notificationsService: NotificationsService = RetrofitInstance.notificationsService
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()


    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = ""
                _shouldRedirectToLogin.value = false

                val response = notificationsService.getUserNotifications("received")

                if (response.isSuccessful) {
                    _notifications.value = response.body()?.notifications?.sortedByDescending { it.timestamp } ?: emptyList()
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = "Error fetching notifications (${response.code()})"
                }

            } catch (e: Exception) {
                _error.value = "Error fetching notifications: ${e.message}"
                Log.e("NotificationsViewModel", "Error fetching notifications", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAllAsRead(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val response = notificationsService.markAllNotificationsAsRead()
                if (response.isSuccessful) {
                    fetchNotifications()
                    onSuccess()
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    Log.e("NotificationsViewModel", "Failed to mark all as read: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error marking all as read", e)
            }
        }
    }
}