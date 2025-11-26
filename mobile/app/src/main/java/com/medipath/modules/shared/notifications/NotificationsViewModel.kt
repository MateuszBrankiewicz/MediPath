package com.medipath.modules.shared.notifications

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import com.medipath.R
import com.medipath.core.models.Notification
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.IOException

class NotificationsViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val notificationsService = RetrofitInstance.notificationsService


    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()
    
    private val context = getApplication<Application>()


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
                    _error.value = context.getString(R.string.error_fetching_notifications)
                }

            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
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
                    _error.value = context.getString(R.string.error_mark_all_as_read)
                }
            } catch (_: IOException) {
                _error.value = context.getString(R.string.error_connection)
            } catch (_: Exception) {
                _error.value = context.getString(R.string.unknown_error)
            }
        }
    }
}