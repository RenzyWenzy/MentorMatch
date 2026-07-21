package edu.cit.estillore.MentorMatch.MentorMatch.review;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.estillore.MentorMatch.MentorMatch.booking.Booking;
import edu.cit.estillore.MentorMatch.MentorMatch.booking.BookingRepository;
import edu.cit.estillore.MentorMatch.MentorMatch.booking.BookingStatus;
import edu.cit.estillore.MentorMatch.MentorMatch.user.User;
import edu.cit.estillore.MentorMatch.MentorMatch.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Review submitReview(String studentEmail, Long bookingId, ReviewRequest request) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found for email: " + studentEmail));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (!booking.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("This booking does not belong to you.");
        }

        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("You can only review sessions that have been completed.");
        }

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new IllegalArgumentException("You've already submitted a review for this session.");
        }

        Review review = Review.builder()
                .booking(booking)
                .student(student)
                .tutorProfile(booking.getTutorProfile())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> findByTutorProfileId(Long tutorProfileId) {
        return reviewRepository.findByTutorProfileIdOrderByCreatedAtDesc(tutorProfileId);
    }

    @Override
    public Double getAverageRating(Long tutorProfileId) {
        Double avg = reviewRepository.findAverageRatingByTutorProfileId(tutorProfileId);
        return avg == null ? null : Math.round(avg * 10) / 10.0;
    }

    @Override
    public long getReviewCount(Long tutorProfileId) {
        return reviewRepository.countByTutorProfileId(tutorProfileId);
    }
}