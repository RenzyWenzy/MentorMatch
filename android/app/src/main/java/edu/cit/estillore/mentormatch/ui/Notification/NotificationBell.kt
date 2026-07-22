package edu.cit.estillore.mentormatch.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import edu.cit.estillore.mentormatch.data.model.Notification

/** Android equivalent of NotificationBell.jsx — badge + dropdown of recent notifications. */
@Composable
fun NotificationBell(viewModel: NotificationViewModel) {
    var open by remember { mutableStateOf(false) }
    val unreadCount by viewModel.unreadCount.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Box {
        BadgedBox(
            badge = {
                if (unreadCount > 0) {
                    Badge(containerColor = MaterialTheme.colorScheme.error) {
                        Text(if (unreadCount > 9) "9+" else unreadCount.toString())
                    }
                }
            }
        ) {
            IconButton(onClick = {
                open = !open
                if (open) viewModel.loadNotifications()
            }) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
        }

        if (open) {
            Popup(alignment = Alignment.TopEnd, onDismissRequest = { open = false }) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.width(320.dp).heightIn(max = 400.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Notifications", style = MaterialTheme.typography.titleSmall)
                            if (unreadCount > 0) {
                                TextButton(onClick = { viewModel.markAllRead() }) {
                                    Text("Mark all read", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        HorizontalDivider()

                        when {
                            loading -> Text("Loading…", modifier = Modifier.padding(12.dp))
                            notifications.isEmpty() -> Text(
                                "No notifications yet.",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            else -> LazyColumn {
                                items(notifications, key = { it.id }) { n ->
                                    NotificationRow(n) { if (!n.read) viewModel.markRead(n.id) }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: Notification, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (notification.read) Color.Transparent else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            .clickable(enabled = !notification.read, onClick = onClick)
            .padding(12.dp)
    ) {
        Text(notification.message, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(2.dp))
        Text(
            notification.createdAt,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}