package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** Backing object for a MENTOR creating/editing their own profile (FR-002). */
@Data
public class TutorProfileRequest {

    @Size(max = 1000)
    private String bio;

    @NotEmpty(message = "At least one subject is required")
    @Valid
    private List<TutorSubjectRequest> subjects;
}
