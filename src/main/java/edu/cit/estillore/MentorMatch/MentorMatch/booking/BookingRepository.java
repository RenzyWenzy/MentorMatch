// BookingRepository.java
package edu.cit.estillore.MentorMatch.MentorMatch.booking;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStudentIdOrderBySessionDateDescStartTimeDesc(Long studentId);

    List<Booking> findByTutorProfileIdOrderBySessionDateDescStartTimeDesc(Long tutorProfileId);

    /** Used to detect double-bookings when validating a new request. */
    List<Booking> findByTutorProfileIdAndSessionDateAndStatusIn(
            Long tutorProfileId, LocalDate sessionDate, List<BookingStatus> statuses);

     /** All other PENDING requests for the same tutor/day, excluding the one just confirmed (FR-007). */
    List<Booking> findByTutorProfileIdAndSessionDateAndStatusAndIdNot(
            Long tutorProfileId, LocalDate sessionDate, BookingStatus status, Long excludedId);

    /** FR-012: guards subject removal — a subject with booking history can't be deleted. */
    boolean existsBySubjectId(Long subjectId);

    /** FR-013: total bookings with a session date in range, regardless of status. */
    long countBySessionDateBetween(LocalDate start, LocalDate end);

    /** FR-013: bookings in a given status with a session date in range. */
    long countByStatusAndSessionDateBetween(BookingStatus status, LocalDate start, LocalDate end);

    /** FR-013: distinct tutors with at least one booking in the given statuses/date range. */
    @Query("SELECT COUNT(DISTINCT b.tutorProfile.id) FROM Booking b " +
            "WHERE b.status IN :statuses AND b.sessionDate BETWEEN :start AND :end")
    long countDistinctActiveTutors(@Param("statuses") List<BookingStatus> statuses,
            @Param("start") LocalDate start, @Param("end") LocalDate end);
}