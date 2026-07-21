package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import edu.cit.estillore.MentorMatch.MentorMatch.availability.Availability;
import edu.cit.estillore.MentorMatch.MentorMatch.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends a MENTOR User with the profile info tutees search against:
 * a short bio plus the subjects/skills/proficiency they teach (FR-002).
 * One-to-one with User — every mentor has at most one of these.
 */
@Entity
@Table(name = "tutor_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 1000)
    private String bio;

    /**
     * BR-002: a newly-created profile is not visible in search until an
     * ADMIN approves it. Defaults to PENDING so this can't be bypassed by
     * forgetting to set it explicitly at creation time.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    /** Set by an ADMIN when rejecting a profile; cleared again on approval. */
    @Column(length = 500)
    private String rejectionReason;

    @Builder.Default
    @OneToMany(mappedBy = "tutorProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TutorSubject> subjects = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tutorProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Availability> availability = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}