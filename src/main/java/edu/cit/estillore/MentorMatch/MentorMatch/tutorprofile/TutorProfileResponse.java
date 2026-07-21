package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import java.util.List;
import java.util.stream.Collectors;

import edu.cit.estillore.MentorMatch.MentorMatch.availability.AvailabilitySlotResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

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
    private Double averageRating; // null if the tutor has no reviews yet (FR-010)
    private long reviewCount;

    /** Convenience overload for call sites that don't have review stats on hand. */
    public static TutorProfileResponse fromEntity(TutorProfile profile) {
        return fromEntity(profile, null, 0);
    }

    public static TutorProfileResponse fromEntity(TutorProfile profile, Double averageRating, long reviewCount) {
        return new TutorProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                profile.getUser().getDepartment(),
                profile.getBio(),
                profile.getSubjects().stream().map(TutorSubjectResponse::fromEntity).collect(Collectors.toList()),
                profile.getAvailability().stream().map(AvailabilitySlotResponse::fromEntity).collect(Collectors.toList()),
                averageRating,
                reviewCount
        );
    }
}