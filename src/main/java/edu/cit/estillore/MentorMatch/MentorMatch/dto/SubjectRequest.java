package edu.cit.estillore.MentorMatch.MentorMatch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Backing object for admin create/edit of a Subject (FR-012). */
@Data
public class SubjectRequest {

    @NotBlank(message = "Subject name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;
}
