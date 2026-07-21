package edu.cit.estillore.MentorMatch.MentorMatch.dto;

import edu.cit.estillore.MentorMatch.MentorMatch.entities.Subject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectResponse {

    private Long id;
    private String name;
    private String description;

    public static SubjectResponse fromEntity(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName(), subject.getDescription());
    }
}
