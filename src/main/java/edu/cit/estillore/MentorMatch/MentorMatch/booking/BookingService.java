// BookingService.java
package edu.cit.estillore.MentorMatch.MentorMatch.booking;

import java.util.List;

public interface BookingService {

    Booking createBooking(String studentEmail, BookingRequest request);

    /** MENTOR: confirms a pending request; auto-declines any other pending requests for the same slot (FR-006, FR-007). */
    Booking confirmBooking(String mentorEmail, Long bookingId);

    Booking declineBooking(String mentorEmail, Long bookingId);

    Booking cancelBooking(String studentEmail, Long bookingId);

    /** MENTOR: marks a confirmed, past-due session as completed (FR-008). */
    Booking completeBooking(String mentorEmail, Long bookingId);

    List<Booking> findOwnBookingsAsStudent(String studentEmail);

    List<Booking> findOwnBookingsAsMentor(String mentorEmail);
}