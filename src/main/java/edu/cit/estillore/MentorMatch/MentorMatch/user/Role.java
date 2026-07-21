package edu.cit.estillore.MentorMatch.MentorMatch.user;

/**
 * The account types supported by the system.
 * Every registered user must be exactly one of these.
 * NOTE: ADMIN accounts are not self-registrable via /api/auth/register —
 * they should be seeded directly in the database or created by an
 * existing admin through a dedicated (secured) endpoint.
 */
public enum Role {
    STUDENT,
    MENTOR,
    ADMIN
}
