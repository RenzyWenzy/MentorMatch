package edu.cit.estillore.MentorMatch.MentorMatch.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Join row between a TutorProfile and a Subject, carrying the tutor's
 * self-reported proficiency for that specific subject (FR-002). A plain
 * ManyToMany can't carry this extra column, hence the explicit entity.
 */
@Entity
@Table(name = "tutor_subjects", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tutor_profile_id", "subject_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_profile_id", nullable = false)
    private TutorProfile tutorProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProficiencyLevel proficiencyLevel;
}
