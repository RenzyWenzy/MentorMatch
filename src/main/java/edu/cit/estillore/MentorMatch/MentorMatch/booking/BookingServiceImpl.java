// BookingServiceImpl.java
package edu.cit.estillore.MentorMatch.MentorMatch.booking;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.estillore.MentorMatch.MentorMatch.notification.NotificationService;
import edu.cit.estillore.MentorMatch.MentorMatch.notification.NotificationType;
import edu.cit.estillore.MentorMatch.MentorMatch.subject.Subject;
import edu.cit.estillore.MentorMatch.MentorMatch.subject.SubjectRepository;
import edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile.TutorProfile;
import edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile.TutorProfileRepository;
import edu.cit.estillore.MentorMatch.MentorMatch.user.Role;
import edu.cit.estillore.MentorMatch.MentorMatch.user.User;
import edu.cit.estillore.MentorMatch.MentorMatch.user.UserRepository;
import lombok.RequiredArgsConstructor;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TutorProfileRepository tutorProfileRepository;
    private final SubjectRepository subjectRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Booking createBooking(String studentEmail, BookingRequest request) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found for email: " + studentEmail));

        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only student accounts can submit booking requests.");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }

        TutorProfile tutorProfile = tutorProfileRepository.findById(request.getTutorProfileId())
                .orElseThrow(() -> new IllegalArgumentException("Tutor profile not found."));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found."));

        boolean tutorTeachesSubject = tutorProfile.getSubjects().stream()
                .anyMatch(ts -> ts.getSubject().getId().equals(subject.getId()));
        if (!tutorTeachesSubject) {
            throw new IllegalArgumentException("This tutor does not teach the selected subject.");
        }

        DayOfWeek requestedDay = request.getSessionDate().getDayOfWeek();
        boolean withinAvailability = tutorProfile.getAvailability().stream()
                .filter(a -> a.getDayOfWeek() == requestedDay)
                .anyMatch(a -> !request.getStartTime().isBefore(a.getStartTime())
                        && !request.getEndTime().isAfter(a.getEndTime()));
        if (!withinAvailability) {
            throw new IllegalArgumentException("Requested time is outside the tutor's availability.");
        }

        validateNoConflict(tutorProfile.getId(), request);

        Booking booking = Booking.builder()
                .student(student)
                .tutorProfile(tutorProfile)
                .subject(subject)
                .sessionDate(request.getSessionDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(BookingStatus.PENDING)
                .build();

        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking confirmBooking(String mentorEmail, Long bookingId) {
        Booking booking = findOwnedPendingBooking(mentorEmail, bookingId);

        // Defense in depth: re-check no other CONFIRMED booking already holds this slot
        // (guards against two near-simultaneous confirmations on overlapping requests).
        boolean alreadyConfirmedElsewhere = bookingRepository
                .findByTutorProfileIdAndSessionDateAndStatusIn(
                        booking.getTutorProfile().getId(), booking.getSessionDate(), List.of(BookingStatus.CONFIRMED))
                .stream()
                .anyMatch(b -> booking.getStartTime().isBefore(b.getEndTime())
                        && b.getStartTime().isBefore(booking.getEndTime()));
        if (alreadyConfirmedElsewhere) {
            throw new IllegalArgumentException("This time slot has already been confirmed for another student.");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setRespondedAt(java.time.LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        // FR-014: let both sides know the session is on.
        notifyBookingEvent(saved, NotificationType.BOOKING_CONFIRMED,
                "Your session with " + tutorName(saved) + " for " + saved.getSubject().getName()
                        + " on " + saved.getSessionDate() + " at " + saved.getStartTime() + " has been confirmed.",
                "You confirmed the session with " + saved.getStudent().getFullName() + " for "
                        + saved.getSubject().getName() + " on " + saved.getSessionDate()
                        + " at " + saved.getStartTime() + ".");

        // FR-007: once confirmed, no other request can hold the same slot —
        // auto-decline every other still-pending request that overlaps it.
        List<Booking> competingPending = bookingRepository.findByTutorProfileIdAndSessionDateAndStatusAndIdNot(
                booking.getTutorProfile().getId(), booking.getSessionDate(), BookingStatus.PENDING, booking.getId());

        for (Booking competitor : competingPending) {
            boolean overlaps = competitor.getStartTime().isBefore(booking.getEndTime())
                    && booking.getStartTime().isBefore(competitor.getEndTime());
            if (overlaps) {
                competitor.setStatus(BookingStatus.DECLINED);
                competitor.setRespondedAt(java.time.LocalDateTime.now());
                Booking declinedCompetitor = bookingRepository.save(competitor);

                // FR-014: the displaced student needs to know why their request was declined.
                notifyBookingEvent(declinedCompetitor, NotificationType.BOOKING_DECLINED,
                        "Your booking request for " + declinedCompetitor.getSubject().getName() + " with "
                                + tutorName(declinedCompetitor) + " on " + declinedCompetitor.getSessionDate()
                                + " was declined because another student's session was confirmed for that time.",
                        "A pending request from " + declinedCompetitor.getStudent().getFullName()
                                + " for " + declinedCompetitor.getSessionDate()
                                + " was automatically declined because it overlapped a session you just confirmed.");
            }
        }

        return saved;
    }

    @Override
    @Transactional
    public Booking completeBooking(String mentorEmail, Long bookingId) {
        TutorProfile profile = tutorProfileRepository.findByUserEmail(mentorEmail)
                .orElseThrow(() -> new IllegalArgumentException("No tutor profile found for this account."));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (!booking.getTutorProfile().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("This booking does not belong to you.");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only confirmed sessions can be marked completed.");
        }

        java.time.LocalDateTime sessionEnd = java.time.LocalDateTime.of(booking.getSessionDate(), booking.getEndTime());
        if (sessionEnd.isAfter(java.time.LocalDateTime.now(ZoneId.of("Asia/Manila")))) {
            throw new IllegalArgumentException("This session hasn't ended yet.");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking declineBooking(String mentorEmail, Long bookingId) {
        Booking booking = findOwnedPendingBooking(mentorEmail, bookingId);
        booking.setStatus(BookingStatus.DECLINED);
        booking.setRespondedAt(java.time.LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        // FR-014
        notifyBookingEvent(saved, NotificationType.BOOKING_DECLINED,
                "Your booking request for " + saved.getSubject().getName() + " with " + tutorName(saved)
                        + " on " + saved.getSessionDate() + " was declined.",
                "You declined the request from " + saved.getStudent().getFullName() + " for "
                        + saved.getSubject().getName() + " on " + saved.getSessionDate() + ".");

        return saved;
    }
    @Override
    @Transactional
    public Booking cancelBooking(String studentEmail, Long bookingId) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found for email: " + studentEmail));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (!booking.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("This booking does not belong to you.");
        }

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("This booking has already been " + booking.getStatus() + ".");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setRespondedAt(java.time.LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        // FR-014
        notifyBookingEvent(saved, NotificationType.BOOKING_CANCELLED,
                "You cancelled your session with " + tutorName(saved) + " for " + saved.getSubject().getName()
                        + " on " + saved.getSessionDate() + ".",
                saved.getStudent().getFullName() + " cancelled their session with you for "
                        + saved.getSubject().getName() + " on " + saved.getSessionDate() + ".");

        return saved;
    }

    @Override
    public List<Booking> findOwnBookingsAsStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found for email: " + studentEmail));
        return bookingRepository.findByStudentIdOrderBySessionDateDescStartTimeDesc(student.getId());
    }

    @Override
    public List<Booking> findOwnBookingsAsMentor(String mentorEmail) {
        TutorProfile profile = tutorProfileRepository.findByUserEmail(mentorEmail)
                .orElseThrow(() -> new IllegalArgumentException("No tutor profile found for this account."));
        return bookingRepository.findByTutorProfileIdOrderBySessionDateDescStartTimeDesc(profile.getId());
    }

    private Booking findOwnedPendingBooking(String mentorEmail, Long bookingId) {
        TutorProfile profile = tutorProfileRepository.findByUserEmail(mentorEmail)
                .orElseThrow(() -> new IllegalArgumentException("No tutor profile found for this account."));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));

        if (!booking.getTutorProfile().getId().equals(profile.getId())) {
            throw new IllegalArgumentException("This booking does not belong to you.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("This booking has already been " + booking.getStatus() + ".");
        }

        return booking;
    }

    /** Rejects a request that overlaps an existing PENDING or ACCEPTED booking for the same tutor/day. */
    private void validateNoConflict(Long tutorProfileId, BookingRequest request) {
        List<Booking> existing = bookingRepository.findByTutorProfileIdAndSessionDateAndStatusIn(
                tutorProfileId, request.getSessionDate(),
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED));

        boolean overlaps = existing.stream().anyMatch(b ->
                request.getStartTime().isBefore(b.getEndTime())
                        && b.getStartTime().isBefore(request.getEndTime()));

        if (overlaps) {
            throw new IllegalArgumentException("This tutor already has a request or session in that time slot.");
        }
    }

    private String tutorName(Booking booking) {
        return booking.getTutorProfile().getUser().getFullName();
    }

    /** FR-014: notifies both the student and the tutor whenever a booking is confirmed, declined, or cancelled. */
    private void notifyBookingEvent(Booking booking, NotificationType type, String studentMessage, String mentorMessage) {
        notificationService.notify(booking.getStudent(), type, studentMessage, booking.getId());
        notificationService.notify(booking.getTutorProfile().getUser(), type, mentorMessage, booking.getId());
    }
}