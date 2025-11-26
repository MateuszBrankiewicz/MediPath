package com.medipath.modules.shared.reminders

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.R
import com.medipath.core.models.AddNotificationRequest
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.launch
import okio.IOException

class AddReminderViewModel(
    application: Application
) : AndroidViewModel(application) {
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

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _shouldRedirectToLogin = mutableStateOf(false)
    val shouldRedirectToLogin: State<Boolean> = _shouldRedirectToLogin
    
    private val context = getApplication<Application>()

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

    fun addReminder(onSuccess: () -> Unit) {

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _shouldRedirectToLogin.value = false

                val request = AddNotificationRequest(
                    userId = null,
                    content = _content.value.ifBlank { null },
                    title = _title.value,
                    startDate = _startDate.value,
                    endDate = _endDate.value.ifBlank { null },
                    reminderTime = _reminderTime.value
                )

                val response = notificationsService.addNotification(request)

                if (response.isSuccessful) {
                    onSuccess()
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = context.getString(R.string.error_add_reminder)
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
}
