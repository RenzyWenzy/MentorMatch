package edu.cit.estillore.MentorMatch.MentorMatch.repository;

import edu.cit.estillore.MentorMatch.MentorMatch.entities.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Subject> findByNameIgnoreCase(String name);
}
