package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutorProfileRepository extends JpaRepository<TutorProfile, Long> {
    Optional<TutorProfile> findByUserId(Long userId);
    Optional<TutorProfile> findByUserEmail(String email);
    boolean existsByUserId(Long userId);
}
