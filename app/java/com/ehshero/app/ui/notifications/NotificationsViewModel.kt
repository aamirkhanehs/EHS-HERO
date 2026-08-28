package com.ehshero.app.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ehshero.app.data.model.AppNotification
import com.ehshero.app.data.remote.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val notifications: List<AppNotification> = emptyList()
)

class NotificationsViewModel(
    private val uid: String,
    private val notificationRepository: NotificationRepository = NotificationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            notificationRepository.observeNotifications(uid).collect { list ->
                _uiState.value = NotificationsUiState(isLoading = false, notifications = list)
            }
        }
    }

    fun markRead(notificationId: String) {
        viewModelScope.launch { notificationRepository.markRead(notificationId) }
    }

    fun markAllRead() {
        viewModelScope.launch { notificationRepository.markAllRead(uid) }
    }
}
