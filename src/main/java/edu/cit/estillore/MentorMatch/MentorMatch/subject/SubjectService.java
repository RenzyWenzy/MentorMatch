package edu.cit.estillore.MentorMatch.MentorMatch.subject;

import java.util.List;

public interface SubjectService {

    List<Subject> findAll();

    Subject create(SubjectRequest request);

    Subject update(Long id, SubjectRequest request);

    void delete(Long id);
}
