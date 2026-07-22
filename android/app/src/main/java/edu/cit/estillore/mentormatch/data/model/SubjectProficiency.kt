package edu.cit.estillore.mentormatch.data.model

import com.squareup.moshi.JsonClass

/** Mirrors the subject+level rows embedded in TutorProfile. */
@JsonClass(generateAdapter = true)
data class SubjectProficiency(
    val subjectId: Long,
    val subjectName: String? = null,
    val proficiencyLevel: ProficiencyLevel
)
