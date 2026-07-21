package edu.cit.estillore.MentorMatch.MentorMatch.review;

import java.util.List;

public interface ReviewService {

    /** STUDENT: rates + reviews a booking the caller submitted, once it's COMPLETED (FR-009). */
    Review submitReview(String studentEmail, Long bookingId, ReviewRequest request);

    List<Review> findByTutorProfileId(Long tutorProfileId);

    /** Average of all ratings for a tutor, rounded to 1 decimal; null if no reviews yet (FR-010). */
    Double getAverageRating(Long tutorProfileId);

    long getReviewCount(Long tutorProfileId);
}