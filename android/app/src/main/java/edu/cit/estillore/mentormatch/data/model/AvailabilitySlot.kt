package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/** Mirrors AvailabilityController.java. */
@JsonClass(generateAdapter = true)
data class AvailabilitySlot(
    val dayOfWeek: DayOfWeek,
    val startTime: String,
    val endTime: String,
    val id: Long? = null
)
