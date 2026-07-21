package edu.cit.estillore.MentorMatch.MentorMatch.dto;

import edu.cit.estillore.MentorMatch.MentorMatch.entities.Availability;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AvailabilitySlotResponse {

    private Long id;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public static AvailabilitySlotResponse fromEntity(Availability a) {
        return new AvailabilitySlotResponse(a.getId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime());
    }
}
