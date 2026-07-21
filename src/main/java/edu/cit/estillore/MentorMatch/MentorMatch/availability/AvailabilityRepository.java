package edu.cit.estillore.MentorMatch.MentorMatch.availability;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByTutorProfileId(Long tutorProfileId);
    void deleteByTutorProfileId(Long tutorProfileId);
}
