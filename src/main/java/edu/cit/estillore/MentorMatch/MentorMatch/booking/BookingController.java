package edu.cit.estillore.MentorMatch.MentorMatch.booking;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.estillore.MentorMatch.MentorMatch.review.ReviewRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final ReviewRepository reviewRepository;

    /** STUDENT-only, enforced in SecurityConfig (FR-005). */
    @PostMapping
    public ResponseEntity<BookingResponse> create(
            Authentication authentication,
            @Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(authentication.getName(), request);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking, false));
    }

    /** MENTOR-only, enforced in SecurityConfig (FR-006). */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirm(Authentication authentication, @PathVariable Long id) {
        Booking booking = bookingService.confirmBooking(authentication.getName(), id);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking, false));
    }

    /** MENTOR-only, enforced in SecurityConfig (FR-006). */
    @PutMapping("/{id}/decline")
    public ResponseEntity<BookingResponse> decline(Authentication authentication, @PathVariable Long id) {
        Booking booking = bookingService.declineBooking(authentication.getName(), id);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking, false));
    }

    /** MENTOR-only: marks a confirmed, past-due session as completed (FR-008). */
    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> complete(Authentication authentication, @PathVariable Long id) {
        Booking booking = bookingService.completeBooking(authentication.getName(), id);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking, false));
    }

    /** STUDENT-only: cancel a booking the caller submitted (before or after acceptance). */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(Authentication authentication, @PathVariable Long id) {
        Booking booking = bookingService.cancelBooking(authentication.getName(), id);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking, false));
    }

    /** STUDENT-only: the caller's own submitted requests. */
    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> ownAsStudent(Authentication authentication) {
        List<Booking> bookings = bookingService.findOwnBookingsAsStudent(authentication.getName());
        Set<Long> reviewedBookingIds = reviewedBookingIds(bookings);

        List<BookingResponse> responses = bookings.stream()
                .map(b -> BookingResponse.fromEntity(b, reviewedBookingIds.contains(b.getId())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** MENTOR-only: requests submitted to the caller's tutor profile. */
    @GetMapping("/tutor/me")
    public ResponseEntity<List<BookingResponse>> ownAsMentor(Authentication authentication) {
        List<Booking> bookings = bookingService.findOwnBookingsAsMentor(authentication.getName());
        Set<Long> reviewedBookingIds = reviewedBookingIds(bookings);

        List<BookingResponse> responses = bookings.stream()
                .map(b -> BookingResponse.fromEntity(b, reviewedBookingIds.contains(b.getId())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /** One query for the whole list instead of one existsByBookingId call per booking. */
    private Set<Long> reviewedBookingIds(List<Booking> bookings) {
        List<Long> ids = bookings.stream().map(Booking::getId).collect(Collectors.toList());
        return Set.copyOf(reviewedBookingIds0(ids));
    }

    private List<Long> reviewedBookingIds0(List<Long> bookingIds) {
        if (bookingIds.isEmpty()) {
            return List.of();
        }
        return reviewRepository.findBookingIdsIn(bookingIds);
    }
}