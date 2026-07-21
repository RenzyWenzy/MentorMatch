package edu.cit.estillore.MentorMatch.MentorMatch.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import edu.cit.estillore.MentorMatch.MentorMatch.booking.BookingRepository;
import edu.cit.estillore.MentorMatch.MentorMatch.booking.BookingStatus;
import edu.cit.estillore.MentorMatch.MentorMatch.review.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public AdminReportResponse generateReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Both startDate and endDate are required.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate.");
        }

        long totalSessions = bookingRepository.countBySessionDateBetween(startDate, endDate);
        long pending = bookingRepository.countByStatusAndSessionDateBetween(BookingStatus.PENDING, startDate, endDate);
        long confirmed = bookingRepository.countByStatusAndSessionDateBetween(BookingStatus.CONFIRMED, startDate, endDate);
        long completed = bookingRepository.countByStatusAndSessionDateBetween(BookingStatus.COMPLETED, startDate, endDate);
        long declined = bookingRepository.countByStatusAndSessionDateBetween(BookingStatus.DECLINED, startDate, endDate);
        long cancelled = bookingRepository.countByStatusAndSessionDateBetween(BookingStatus.CANCELLED, startDate, endDate);

        long activeTutors = bookingRepository.countDistinctActiveTutors(
                List.of(BookingStatus.CONFIRMED, BookingStatus.COMPLETED), startDate, endDate);

        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = endDate.atTime(LocalTime.MAX);

        long newReviews = reviewRepository.countByCreatedAtBetween(rangeStart, rangeEnd);
        Double avgRating = reviewRepository.findAverageRatingBetween(rangeStart, rangeEnd);
        Double roundedAvg = avgRating == null ? null : Math.round(avgRating * 10) / 10.0;

        return new AdminReportResponse(
                startDate, endDate,
                totalSessions, pending, confirmed, completed, declined, cancelled,
                activeTutors, newReviews, roundedAvg);
    }
}