package edu.cit.estillore.MentorMatch.MentorMatch.repository;

import edu.cit.estillore.MentorMatch.MentorMatch.entities.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByTutorProfileId(Long tutorProfileId);
    void deleteByTutorProfileId(Long tutorProfileId);
}
