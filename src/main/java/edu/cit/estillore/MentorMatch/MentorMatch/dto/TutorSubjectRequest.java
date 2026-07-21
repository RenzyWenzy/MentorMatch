package edu.cit.estillore.MentorMatch.MentorMatch.dto;

import edu.cit.estillore.MentorMatch.MentorMatch.entities.ProficiencyLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** One subject a tutor teaches, with their self-reported proficiency. */
@Data
public class TutorSubjectRequest {

    @NotNull(message = "Subject is required")
    private Long subjectId;

    @NotNull(message = "Proficiency level is required")
    private ProficiencyLevel proficiencyLevel;
}
