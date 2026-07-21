package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TutorSubjectRepository extends JpaRepository<TutorSubject, Long> {
    List<TutorSubject> findBySubjectId(Long subjectId);
    void deleteByTutorProfileId(Long tutorProfileId);
}
