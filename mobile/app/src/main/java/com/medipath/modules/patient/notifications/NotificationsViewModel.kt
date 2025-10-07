package com.medipath.modules.patient.notifications

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.util.Log
import com.medipath.core.models.Notification
import com.medipath.core.services.UserService
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.network.RetrofitInstance

class NotificationsViewModel(
    private val userService: UserService = RetrofitInstance.userService
) : ViewModel() {

    private val _notifications = mutableStateOf<List<Notification>>(emptyList())
    val notifications: State<List<Notification>> = _notifications

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf("")
    val error: State<String> = _error

    fun fetchNotifications(sessionManager: DataStoreSessionManager) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = ""

                val token = sessionManager.getSessionId()
                if (token.isNullOrEmpty()) {
                    _error.value = "No session ID found"
                    return@launch
                }

                val profileResponse = userService.getNotificationsFromProfile("SESSION=$token")
                _notifications.value = profileResponse.user.notifications

            } catch (e: Exception) {
                _error.value = "Error fetching notifications: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = ""
    }

    fun markAllAsRead(sessionManager: DataStoreSessionManager) {
        viewModelScope.launch {
            try {
                val token = sessionManager.getSessionId()
                if (token.isNullOrEmpty()) return@launch

                // TODO: Dodaj endpint oznacz wszstkie jako przczytane
                _notifications.value = _notifications.value.map { it.copy(read = true) }
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error marking all as read: $e")
            }
        }
    }


}