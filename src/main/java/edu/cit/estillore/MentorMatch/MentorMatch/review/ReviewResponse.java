package edu.cit.estillore.MentorMatch.MentorMatch.review;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Long bookingId;
    private Long tutorProfileId;
    private Long studentId;
    private String studentName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public static ReviewResponse fromEntity(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getBooking().getId(),
                r.getTutorProfile().getId(),
                r.getStudent().getId(),
                r.getStudent().getFullName(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}