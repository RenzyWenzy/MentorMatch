package edu.cit.estillore.MentorMatch.MentorMatch.service;

import edu.cit.estillore.MentorMatch.MentorMatch.dto.SubjectRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.entities.Subject;

import java.util.List;

public interface SubjectService {

    List<Subject> findAll();

    Subject create(SubjectRequest request);

    Subject update(Long id, SubjectRequest request);

    void delete(Long id);
}
