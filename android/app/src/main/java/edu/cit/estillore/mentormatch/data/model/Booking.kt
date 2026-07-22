package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/**
 * ASSUMPTION: field names are inferred from MentorDashboard.jsx's booking
 * table (studentName, subjectName, sessionDate, startTime, endTime, status).
 * tutorName is added for the student-side list; verify against the backend
 * BookingResponse DTO if it differs.
 */
@JsonClass(generateAdapter = true)
data class Booking(
    val id: Long,
    val studentName: String? = null,
    val tutorName: String? = null,
    val subjectId: Long? = null,
    val subjectName: String,
    val sessionDate: String,
    val startTime: String,
    val endTime: String,
    val status: BookingStatus
)