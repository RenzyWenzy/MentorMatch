// BookingRepository.java
package edu.cit.estillore.MentorMatch.MentorMatch.booking;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStudentIdOrderBySessionDateDescStartTimeDesc(Long studentId);

    List<Booking> findByTutorProfileIdOrderBySessionDateDescStartTimeDesc(Long tutorProfileId);

    /** Used to detect double-bookings when validating a new request. */
    List<Booking> findByTutorProfileIdAndSessionDateAndStatusIn(
            Long tutorProfileId, LocalDate sessionDate, List<BookingStatus> statuses);
            
     /** All other PENDING requests for the same tutor/day, excluding the one just confirmed (FR-007). */
    List<Booking> findByTutorProfileIdAndSessionDateAndStatusAndIdNot(
            Long tutorProfileId, LocalDate sessionDate, BookingStatus status, Long excludedId);
}