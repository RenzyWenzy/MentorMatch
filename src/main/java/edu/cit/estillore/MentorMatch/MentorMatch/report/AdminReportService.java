package edu.cit.estillore.MentorMatch.MentorMatch.report;

import java.time.LocalDate;

public interface AdminReportService {

    /**
     * ADMIN: aggregates sessions (by status), submitted reviews/average rating,
     * and active-tutor counts over [startDate, endDate], inclusive (FR-013).
     */
    AdminReportResponse generateReport(LocalDate startDate, LocalDate endDate);
}