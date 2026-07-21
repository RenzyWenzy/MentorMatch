package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

/**
 * Review state of a TutorProfile (BR-002). A new profile starts PENDING and
 * must be approved by an ADMIN before it appears in public search results.
 */
public enum ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}