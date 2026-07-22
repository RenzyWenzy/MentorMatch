package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/**
 * ASSUMPTION: StudentDashboard.jsx (the booking-creation form) wasn't
 * available, so this payload is inferred from the availability/subject
 * model shape rather than confirmed against the web form. Double check
 * field names against the backend BookingRequest DTO or StudentDashboard.jsx.
 */
@JsonClass(generateAdapter = true)
data class BookingRequest(
    val tutorProfileId: Long,
    val subjectId: Long,
    val sessionDate: String,
    val startTime: String,
    val endTime: String
)