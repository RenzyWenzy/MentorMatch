package edu.cit.estillore.mentormatch.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cit.estillore.mentormatch.data.model.Notification
import edu.cit.estillore.mentormatch.data.repository.NotificationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Mirrors NotificationBell.jsx: 30s polling for the unread badge, load-on-open for the list. */
private const val POLL_MS = 30_000L

class NotificationViewModel(private val notificationRepository: NotificationRepository) : ViewModel() {

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        refreshUnreadCount()
        viewModelScope.launch {
            while (true) {
                delay(POLL_MS)
                refreshUnreadCount()
            }
        }
    }

    private fun refreshUnreadCount() {
        viewModelScope.launch {
            notificationRepository.unreadCount().onSuccess { _unreadCount.value = it }
            // Silently ignore failure — the badge just won't update this cycle.
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _loading.value = true
            notificationRepository.mine().onSuccess { _notifications.value = it }
            _loading.value = false
        }
    }

    fun markRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markRead(id).onSuccess {
                _notifications.value = _notifications.value.map { n -> if (n.id == id) n.copy(read = true) else n }
                _unreadCount.value = (_unreadCount.value - 1).coerceAtLeast(0)
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            notificationRepository.markAllRead().onSuccess {
                _notifications.value = _notifications.value.map { it.copy(read = true) }
                _unreadCount.value = 0
            }
        }
    }

    class Factory(private val notificationRepository: NotificationRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel(notificationRepository) as T
        }
    }
}