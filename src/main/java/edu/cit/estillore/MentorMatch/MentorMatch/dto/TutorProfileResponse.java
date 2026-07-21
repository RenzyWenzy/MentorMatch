package edu.cit.estillore.MentorMatch.MentorMatch.dto;

import edu.cit.estillore.MentorMatch.MentorMatch.entities.TutorProfile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class TutorProfileResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String department;
    private String bio;
    private List<TutorSubjectResponse> subjects;
    private List<AvailabilitySlotResponse> availability;

    public static TutorProfileResponse fromEntity(TutorProfile profile) {
        return new TutorProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                profile.getUser().getDepartment(),
                profile.getBio(),
                profile.getSubjects().stream().map(TutorSubjectResponse::fromEntity).collect(Collectors.toList()),
                profile.getAvailability().stream().map(AvailabilitySlotResponse::fromEntity).collect(Collectors.toList())
        );
    }
}
