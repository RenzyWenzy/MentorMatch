package edu.cit.estillore.mentormatch.data.repository

import edu.cit.estillore.mentormatch.data.api.ErrorParser
import edu.cit.estillore.mentormatch.data.api.NotificationApi
import edu.cit.estillore.mentormatch.data.model.Notification

class NotificationRepository(private val notificationApi: NotificationApi) {

    suspend fun mine(): Result<List<Notification>> = runCatching {
        val response = notificationApi.mine()
        if (response.isSuccessful) response.body() ?: emptyList()
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    suspend fun unreadCount(): Result<Int> = runCatching {
        val response = notificationApi.unreadCount()
        if (response.isSuccessful) response.body()?.count ?: 0
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    suspend fun markRead(id: Long): Result<Notification> = runCatching {
        val response = notificationApi.markRead(id)
        if (response.isSuccessful) response.body() ?: error("Empty response from server.")
        else throw IllegalStateException(ErrorParser.parse(response))
    }

    suspend fun markAllRead(): Result<Unit> = runCatching {
        val response = notificationApi.markAllRead()
        if (!response.isSuccessful) throw IllegalStateException(ErrorParser.parse(response))
    }
}