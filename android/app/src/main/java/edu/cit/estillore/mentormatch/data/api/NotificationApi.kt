package edu.cit.estillore.mentormatch.data.api

import edu.cit.estillore.mentormatch.data.model.Notification
import edu.cit.estillore.mentormatch.data.model.UnreadCountResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/** Matches NotificationController.java. */
interface NotificationApi {

    @GET("api/notifications/me")
    suspend fun mine(): Response<List<Notification>>

    @GET("api/notifications/me/unread-count")
    suspend fun unreadCount(): Response<UnreadCountResponse>

    @PUT("api/notifications/{id}/read")
    suspend fun markRead(@Path("id") id: Long): Response<Notification>

    @PUT("api/notifications/read-all")
    suspend fun markAllRead(): Response<Unit>
}