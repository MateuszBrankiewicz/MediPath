package com.medipath.modules.patient.reminders

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.AddNotificationRequest
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.launch

class AddReminderViewModel : ViewModel() {
    private val notificationsService = RetrofitInstance.notificationsService

    private val _title = mutableStateOf("")
    val title: State<String> = _title

    private val _content = mutableStateOf("")
    val content: State<String> = _content

    private val _startDate = mutableStateOf("")
    val startDate: State<String> = _startDate

    private val _endDate = mutableStateOf("")
    val endDate: State<String> = _endDate

    private val _reminderTime = mutableStateOf("")
    val reminderTime: State<String> = _reminderTime

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun updateTitle(value: String) {
        _title.value = value
    }

    fun updateContent(value: String) {
        _content.value = value
    }

    fun updateStartDate(value: String) {
        _startDate.value = value
    }

    fun updateEndDate(value: String) {
        _endDate.value = value
    }

    fun updateReminderTime(value: String) {
        _reminderTime.value = value
    }

    fun addReminder(sessionManager: DataStoreSessionManager, onSuccess: () -> Unit) {

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val sessionId = sessionManager.getSessionId()
                if (sessionId == null) {
                    _isLoading.value = false
                    return@launch
                }

                val request = AddNotificationRequest(
                    userId = null,
                    content = if (_content.value.isBlank()) null else _content.value,
                    title = _title.value,
                    startDate = _startDate.value,
                    endDate = if (_endDate.value.isBlank()) null else _endDate.value,
                    reminderTime = _reminderTime.value
                )

                notificationsService.addNotification(request, "SESSION=$sessionId")

                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                Log.e("AddReminderViewModel", "Error adding reminder", e)
                _isLoading.value = false
            }
        }
    }
}
