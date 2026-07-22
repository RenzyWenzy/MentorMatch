package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/**
 * Mirrors the tutor-profile response shape used across /me, /search,
 * /tutor-profiles (list) and /pending. Some fields are only populated in
 * certain contexts (e.g. fullName/email/department show up in the pending
 * approval queue and search results, not necessarily on /me).
 */
@JsonClass(generateAdapter = true)
data class TutorProfile(
    val id: Long,
    val fullName: String? = null,
    val email: String? = null,
    val department: String? = null,
    val bio: String? = null,
    val subjects: List<SubjectProficiency> = emptyList(),
    val availability: List<AvailabilitySlot> = emptyList(),
    val approvalStatus: ApprovalStatus? = null,
    val rejectionReason: String? = null,
    val averageRating: Double? = null,
    val reviewCount: Int = 0
)