package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/** Mirrors NotificationController.java. */
@JsonClass(generateAdapter = true)
data class Notification(
    val id: Long,
    val message: String,
    val read: Boolean = false,
    val createdAt: String
)
