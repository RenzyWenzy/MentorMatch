package edu.cit.estillore.MentorMatch.MentorMatch.availability;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** One weekly recurring slot, e.g. MONDAY 14:00-16:00 (FR-003). */
@Data
public class AvailabilitySlotRequest {

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;
}
