// BookingRequest.java
package edu.cit.estillore.MentorMatch.MentorMatch.booking;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Backing object for a student submitting a booking request (FR-005). */
@Data
public class BookingRequest {

    @NotNull(message = "Tutor is required")
    private Long tutorProfileId;

    @NotNull(message = "Subject is required")
    private Long subjectId;

    @NotNull(message = "Session date is required")
    @FutureOrPresent(message = "Session date cannot be in the past")
    private LocalDate sessionDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}