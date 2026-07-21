// BookingController.java
package edu.cit.estillore.MentorMatch.MentorMatch.booking;

import java.util.List;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** STUDENT-only, enforced in SecurityConfig (FR-005). */
    @PostMapping
    public ResponseEntity<BookingResponse> create(
            Authentication authentication,
            @Valid @RequestBody BookingRequest request) {
        Booking booking = bookingService.createBooking(authentication.getName(), request);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }

    /** MENTOR-only, enforced in SecurityConfig (FR-006). */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirm(Authentication authentication, @PathVariable Long id) {
        Booking booking = bookingService.confirmBooking(authentication.getName(), id);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }

    /** MENTOR-only, enforced in SecurityConfig (FR-006). */
    @PutMapping("/{id}/decline")
    public ResponseEntity<BookingResponse> decline(Authentication authentication, @PathVariable Long id) {
        Booking booking = bookingService.declineBooking(authentication.getName(), id);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }

    /** MENTOR-only: marks a confirmed, past-due session as completed (FR-008). */
    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> complete(Authentication authentication, @PathVariable Long id) {
        Booking booking = bookingService.completeBooking(authentication.getName(), id);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }
    
    /** STUDENT-only: cancel a booking the caller submitted (before or after acceptance). */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(Authentication authentication, @PathVariable Long id) {
        Booking booking = bookingService.cancelBooking(authentication.getName(), id);
        return ResponseEntity.ok(BookingResponse.fromEntity(booking));
    }
    /** STUDENT-only: the caller's own submitted requests. */
    @GetMapping("/me")
    public ResponseEntity<List<BookingResponse>> ownAsStudent(Authentication authentication) {
        List<BookingResponse> bookings = bookingService.findOwnBookingsAsStudent(authentication.getName()).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }

    /** MENTOR-only: requests submitted to the caller's tutor profile. */
    @GetMapping("/tutor/me")
    public ResponseEntity<List<BookingResponse>> ownAsMentor(Authentication authentication) {
        List<BookingResponse> bookings = bookingService.findOwnBookingsAsMentor(authentication.getName()).stream()
                .map(BookingResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookings);
    }
}