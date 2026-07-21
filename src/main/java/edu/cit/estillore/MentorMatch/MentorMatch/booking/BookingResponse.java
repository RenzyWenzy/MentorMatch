// BookingResponse.java
package edu.cit.estillore.MentorMatch.MentorMatch.booking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingResponse {

    private Long id;
    private Long tutorProfileId;
    private String tutorName;
    private Long studentId;
    private String studentName;
    private Long subjectId;
    private String subjectName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private boolean hasReview;

    public static BookingResponse fromEntity(Booking b, boolean hasReview) {
        return new BookingResponse(
                b.getId(),
                b.getTutorProfile().getId(),
                b.getTutorProfile().getUser().getFullName(),
                b.getStudent().getId(),
                b.getStudent().getFullName(),
                b.getSubject().getId(),
                b.getSubject().getName(),
                b.getSessionDate(),
                b.getStartTime(),
                b.getEndTime(),
                b.getStatus(),
                b.getCreatedAt(),
                b.getRespondedAt(),
                hasReview
        );
    }
}