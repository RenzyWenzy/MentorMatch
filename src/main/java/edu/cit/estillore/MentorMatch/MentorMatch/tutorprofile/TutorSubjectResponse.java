package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TutorSubjectResponse {

    private Long subjectId;
    private String subjectName;
    private ProficiencyLevel proficiencyLevel;

    public static TutorSubjectResponse fromEntity(TutorSubject ts) {
        return new TutorSubjectResponse(
                ts.getSubject().getId(),
                ts.getSubject().getName(),
                ts.getProficiencyLevel()
        );
    }
}
