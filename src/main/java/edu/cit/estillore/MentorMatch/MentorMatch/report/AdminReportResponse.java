package edu.cit.estillore.MentorMatch.MentorMatch.report;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

/** FR-013: aggregate tutoring-activity stats for an ADMIN-selected date range. */
@Data
@AllArgsConstructor
public class AdminReportResponse {

    private LocalDate startDate;
    private LocalDate endDate;

    private long totalSessions;
    private long pendingCount;
    private long confirmedCount;
    private long completedCount;
    private long declinedCount;
    private long cancelledCount;

    /** Distinct tutors with at least one confirmed or completed session in range. */
    private long activeTutorsCount;

    private long newReviewsCount;

    /** Average of all ratings submitted in range, rounded to 1 decimal; null if none were submitted. */
    private Double averageRating;
}