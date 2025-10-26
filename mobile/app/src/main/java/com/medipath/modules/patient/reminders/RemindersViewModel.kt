package com.medipath.modules.patient.reminders

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.MarkNotificationReadRequest
import com.medipath.core.models.Notification
import com.medipath.core.network.DataStoreSessionManager
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RemindersViewModel : ViewModel() {
    private val notificationsService = RetrofitInstance.notificationsService

    private val _reminders = mutableStateOf<List<Notification>>(emptyList())
    val reminders: State<List<Notification>> = _reminders

    private val _filteredReminders = mutableStateOf<List<Notification>>(emptyList())
    val filteredReminders: State<List<Notification>> = _filteredReminders

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    // Filter states
    private val _statusFilter = mutableStateOf("All")
    val statusFilter: State<String> = _statusFilter

    private val _dateFromFilter = mutableStateOf("")
    val dateFromFilter: State<String> = _dateFromFilter

    private val _dateToFilter = mutableStateOf("")
    val dateToFilter: State<String> = _dateToFilter

    private val _sortBy = mutableStateOf("Date (Newest first)")
    val sortBy: State<String> = _sortBy

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    // Statistics
    private val _totalReminders = mutableStateOf(0)
    val totalReminders: State<Int> = _totalReminders

    private val _unreadReminders = mutableStateOf(0)
    val unreadReminders: State<Int> = _unreadReminders

    private val _todayReminders = mutableStateOf(0)
    val todayReminders: State<Int> = _todayReminders

    fun fetchReminders(sessionManager: DataStoreSessionManager) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val sessionId = sessionManager.getSessionId()
                if (sessionId == null) {
                    _error.value = "No session found"
                    _isLoading.value = false
                    return@launch
                }

                val response = notificationsService.getUserNotifications("SESSION=$sessionId")

                _reminders.value = response.notifications
                updateStatistics()
                applyFilters()
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("RemindersViewModel", "Error fetching reminders", e)
                _error.value = "Failed to load reminders: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notification: Notification, sessionManager: DataStoreSessionManager) {
        viewModelScope.launch {
            try {
                val sessionId = sessionManager.getSessionId()

                val request = MarkNotificationReadRequest(
                    timestamp = notification.timestamp,
                    title = notification.title
                )

                notificationsService.markNotificationAsRead(request, "SESSION=$sessionId")

                _reminders.value = _reminders.value.map {
                    if (it.timestamp == notification.timestamp && it.title == notification.title) {
                        it.copy(read = true)
                    } else {
                        it
                    }
                }
                updateStatistics()
                applyFilters()
            } catch (e: Exception) {
                Log.e("RemindersViewModel", "Error marking as read", e)
                _error.value = "Failed to mark as read: ${e.message}"
            }
        }
    }

    fun markAllAsRead(sessionManager: DataStoreSessionManager) {
        viewModelScope.launch {
            try {
                val sessionId = sessionManager.getSessionId()

                notificationsService.markAllNotificationsAsRead("SESSION=$sessionId")

                _reminders.value = _reminders.value.map { it.copy(read = true) }
                updateStatistics()
                applyFilters()
            } catch (e: Exception) {
                Log.e("RemindersViewModel", "Error marking all as read", e)
                _error.value = "Failed to mark all as read: ${e.message}"
            }
        }
    }

    fun deleteReminder(notification: Notification) {
        _reminders.value = _reminders.value.filter {
            !(it.timestamp == notification.timestamp && it.title == notification.title)
        }
        updateStatistics()
        applyFilters()
    }

    fun updateStatusFilter(status: String) {
        _statusFilter.value = status
        applyFilters()
    }

    fun updateDateFromFilter(date: String) {
        _dateFromFilter.value = date
        applyFilters()
    }

    fun updateDateToFilter(date: String) {
        _dateToFilter.value = date
        applyFilters()
    }

    fun updateSortBy(sort: String) {
        _sortBy.value = sort
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun clearFilters() {
        _statusFilter.value = "All"
        _dateFromFilter.value = ""
        _dateToFilter.value = ""
        _sortBy.value = "Date (Newest first)"
        _searchQuery.value = ""
        applyFilters()
    }

    private fun updateStatistics() {
        _totalReminders.value = _reminders.value.size
        _unreadReminders.value = _reminders.value.count { !it.read }

        val today = LocalDate.now()
        _todayReminders.value = _reminders.value.count { notification ->
            try {
                val notificationDate = LocalDateTime.parse(
                    notification.timestamp,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                ).toLocalDate()
                notificationDate.isEqual(today)
            } catch (e: Exception) {
                false
            }
        }
    }

    private fun applyFilters() {
        var filtered = _reminders.value

        filtered = when (_statusFilter.value) {
            "Read" -> filtered.filter { it.read }
            "Unread" -> filtered.filter { !it.read }
            else -> filtered
        }

        if (_dateFromFilter.value.isNotEmpty()) {
            try {
                val fromDate = LocalDate.parse(_dateFromFilter.value, DateTimeFormatter.ISO_LOCAL_DATE)
                filtered = filtered.filter { notification ->
                    try {
                        val notificationDate = LocalDateTime.parse(
                            notification.timestamp,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        ).toLocalDate()
                        !notificationDate.isBefore(fromDate)
                    } catch (e: Exception) {
                        true
                    }
                }
            } catch (e: Exception) {
            }
        }

        if (_dateToFilter.value.isNotEmpty()) {
            try {
                val toDate = LocalDate.parse(_dateToFilter.value, DateTimeFormatter.ISO_LOCAL_DATE)
                filtered = filtered.filter { notification ->
                    try {
                        val notificationDate = LocalDateTime.parse(
                            notification.timestamp,
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        ).toLocalDate()
                        !notificationDate.isAfter(toDate)
                    } catch (e: Exception) {
                        true
                    }
                }
            } catch (e: Exception) {
            }
        }

        if (_searchQuery.value.isNotEmpty()) {
            val query = _searchQuery.value.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(query) ||
                it.content.lowercase().contains(query)
            }
        }

        filtered = when (_sortBy.value) {
            "Date (Newest first)" -> filtered.sortedByDescending { it.timestamp }
            "Date (Oldest first)" -> filtered.sortedByDescending { it.timestamp }.reversed()
            "Title (A-Z)" -> filtered.sortedBy { it.title }
            "Title (Z-A)" -> filtered.sortedBy { it.title }.reversed()
            else -> filtered
        }

        _filteredReminders.value = filtered
    }
}
