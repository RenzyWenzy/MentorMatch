package edu.cit.estillore.MentorMatch.MentorMatch.review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByTutorProfileIdOrderByCreatedAtDesc(Long tutorProfileId);

    /** Guards against a student reviewing the same booking twice. */
    boolean existsByBookingId(Long bookingId);

    long countByTutorProfileId(Long tutorProfileId);

    /** Null when the tutor has no reviews yet (FR-010). */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tutorProfile.id = :tutorProfileId")
    Double findAverageRatingByTutorProfileId(@Param("tutorProfileId") Long tutorProfileId);
}