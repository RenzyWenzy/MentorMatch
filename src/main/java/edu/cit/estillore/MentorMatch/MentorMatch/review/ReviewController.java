package edu.cit.estillore.MentorMatch.MentorMatch.review;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** STUDENT-only, enforced in SecurityConfig (FR-009). */
    @PostMapping("/api/bookings/{id}/review")
    public ResponseEntity<ReviewResponse> submitReview(
            Authentication authentication,
            @PathVariable("id") Long bookingId,
            @Valid @RequestBody ReviewRequest request) {
        Review review = reviewService.submitReview(authentication.getName(), bookingId, request);
        return ResponseEntity.ok(ReviewResponse.fromEntity(review));
    }

    /** Any authenticated role — students browsing tutors need to see feedback too. */
    @GetMapping("/api/tutor-profiles/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getByTutorProfile(@PathVariable("id") Long tutorProfileId) {
        List<ReviewResponse> reviews = reviewService.findByTutorProfileId(tutorProfileId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(reviews);
    }
}