package edu.cit.estillore.MentorMatch.MentorMatch.review;

import java.time.LocalDateTime;

import edu.cit.estillore.MentorMatch.MentorMatch.booking.Booking;
import edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile.TutorProfile;
import edu.cit.estillore.MentorMatch.MentorMatch.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A student's rating + written feedback for a COMPLETED booking (FR-009).
 * One review per booking, enforced by the unique constraint on booking_id.
 * tutorProfile is denormalized onto the row (rather than reached via
 * booking.getTutorProfile()) purely so FR-010's average-rating query can
 * filter directly on it without an extra join.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = "booking_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_profile_id", nullable = false)
    private TutorProfile tutorProfile;

    /** 1-5, enforced by @Min/@Max on ReviewRequest before this entity is built. */
    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}