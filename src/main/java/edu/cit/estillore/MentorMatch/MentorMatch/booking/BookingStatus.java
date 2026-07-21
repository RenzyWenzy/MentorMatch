// BookingStatus.java
package edu.cit.estillore.MentorMatch.MentorMatch.booking;

/** Lifecycle of a session booking request (FR-005, FR-006). */
public enum BookingStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}