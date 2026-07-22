package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/**
 * Mirrors AdminReportController.java. Field names inferred from
 * AdminDashboardScreen.kt's ReportsTab usage — verify against the backend
 * AdminReport DTO if it differs.
 */
@JsonClass(generateAdapter = true)
data class AdminReport(
    val totalSessions: Int = 0,
    val completedCount: Int = 0,
    val confirmedCount: Int = 0,
    val activeTutorsCount: Int = 0,
    val pendingCount: Int = 0,
    val declinedCount: Int = 0,
    val cancelledCount: Int = 0,
    val newReviewsCount: Int = 0,
    val averageRating: Double? = null
)
