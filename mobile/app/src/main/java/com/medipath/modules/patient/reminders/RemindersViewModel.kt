package com.medipath.modules.patient.reminders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medipath.core.models.MarkNotificationReadRequest
import com.medipath.core.models.Notification
import com.medipath.core.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RemindersViewModel : ViewModel() {
    private val notificationsService = RetrofitInstance.notificationsService

    private val _reminders = MutableStateFlow<List<Notification>>(emptyList())
    val reminders: StateFlow<List<Notification>> = _reminders.asStateFlow()

    private val _filteredReminders = MutableStateFlow<List<Notification>>(emptyList())
    val filteredReminders: StateFlow<List<Notification>> = _filteredReminders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedTab = MutableStateFlow("received")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _dateFromFilter = MutableStateFlow("")
    val dateFromFilter: StateFlow<String> = _dateFromFilter.asStateFlow()

    private val _dateToFilter = MutableStateFlow("")
    val dateToFilter: StateFlow<String> = _dateToFilter.asStateFlow()

    private val _sortBy = MutableStateFlow("Date")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private val _sortOrder = MutableStateFlow("Descending")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _totalReminders = MutableStateFlow(0)
    val totalReminders: StateFlow<Int> = _totalReminders.asStateFlow()

    private val _unreadReminders = MutableStateFlow(0)
    val unreadReminders: StateFlow<Int> = _unreadReminders.asStateFlow()

    private val _todayReminders = MutableStateFlow(0)
    val todayReminders: StateFlow<Int> = _todayReminders.asStateFlow()

    private val _shouldRedirectToLogin = MutableStateFlow(false)
    val shouldRedirectToLogin: StateFlow<Boolean> = _shouldRedirectToLogin.asStateFlow()

    fun fetchReminders() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _shouldRedirectToLogin.value = false

                val response = notificationsService.getUserNotifications(_selectedTab.value)

                if (response.isSuccessful) {
                    _reminders.value = response.body()?.notifications ?: emptyList()
                    updateStatistics()
                    applyFilters()
                } else if (response.code() == 401) {
                    _shouldRedirectToLogin.value = true
                } else {
                    _error.value = "Failed to load reminders: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load reminders: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notification: Notification, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val request = MarkNotificationReadRequest(
                    timestamp = notification.timestamp,
                    title = notification.title
                )

                val response = notificationsService.markNotificationAsRead(request)

                if (response.isSuccessful) {
                    fetchReminders()
                    onSuccess()
                } else {
                    when (response.code()) {
                        401 -> _shouldRedirectToLogin.value = true
                        else -> _error.value = "Failed to mark as read: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to mark as read: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAllAsRead(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = notificationsService.markAllNotificationsAsRead()

                if (response.isSuccessful) {
                    fetchReminders()
                    onSuccess()
                } else {
                    when (response.code()) {
                        401 -> _error.value = "Unauthorized"
                        else -> _error.value = "Failed to mark all as read: ${response.code()}"
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("RemindersViewModel", "Error marking all as read", e)
                _error.value = "Failed to mark all as read: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteReminder(notification: Notification) {
        viewModelScope.launch {
            try {
                val parsed = LocalDateTime.parse(notification.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                val request = com.medipath.core.models.DeleteNotificationsRequest(
                    title = notification.title,
                    reminderTime = parsed.toLocalTime().toString(),
                    startDate = parsed.toLocalDate().toString(),
                    endDate = parsed.toLocalDate().toString()
                )

                val response = notificationsService.deleteNotifications(request)

                if (response.isSuccessful) {
                    fetchReminders()
                } else {
                    when (response.code()) {
                        401 -> _error.value = "Unauthorized"
                        400 -> _error.value = "Bad request: missing field or no notification matches criteria"
                        else -> _error.value = "Failed to delete reminder: ${response.code()}"
                    }
                }
            } catch (e: Exception) {
                Log.e("RemindersViewModel", "Error deleting reminder", e)
                _error.value = "Failed to delete reminder: ${e.message}"
            }
        }
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

    fun updateSortOrder(order: String) {
        _sortOrder.value = order
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updateSelectedTab(tab: String) {
        _selectedTab.value = tab
        applyFilters()
    }

    fun clearFilters() {
        _statusFilter.value = "All"
        _dateFromFilter.value = ""
        _dateToFilter.value = ""
        _sortBy.value = "Date"
        _sortOrder.value = "Descending"
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
            "Date" -> {
                if (_sortOrder.value == "Ascending") {
                    filtered.sortedBy { it.timestamp }
                } else {
                    filtered.sortedByDescending { it.timestamp }
                }
            }
            "Title" -> {
                if (_sortOrder.value == "Ascending") {
                    filtered.sortedBy { it.title }
                } else {
                    filtered.sortedByDescending { it.title }
                }
            }
            else -> filtered
        }

        _filteredReminders.value = filtered
    }
}
