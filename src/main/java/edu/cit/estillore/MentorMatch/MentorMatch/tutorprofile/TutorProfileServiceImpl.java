package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.estillore.MentorMatch.MentorMatch.subject.Subject;
import edu.cit.estillore.MentorMatch.MentorMatch.subject.SubjectRepository;
import edu.cit.estillore.MentorMatch.MentorMatch.user.Role;
import edu.cit.estillore.MentorMatch.MentorMatch.user.User;
import edu.cit.estillore.MentorMatch.MentorMatch.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TutorProfileServiceImpl implements TutorProfileService {

    private final TutorProfileRepository tutorProfileRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional
    public TutorProfile createOrUpdateOwnProfile(String mentorEmail, TutorProfileRequest request) {
        User user = userRepository.findByEmail(mentorEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found for email: " + mentorEmail));

        if (user.getRole() != Role.MENTOR) {
            throw new IllegalArgumentException("Only mentor accounts can have a tutor profile.");
        }

        TutorProfile profile = tutorProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> TutorProfile.builder().user(user).subjects(new ArrayList<>()).build());

        profile.setBio(request.getBio());

        // Replace the subject list wholesale — simpler and safer than
        // diffing individual add/remove operations from the client.
        profile.getSubjects().clear();
        for (TutorSubjectRequest ts : request.getSubjects()) {
            Subject subject = subjectRepository.findById(ts.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found: id=" + ts.getSubjectId()));
            profile.getSubjects().add(TutorSubject.builder()
                    .tutorProfile(profile)
                    .subject(subject)
                    .proficiencyLevel(ts.getProficiencyLevel())
                    .build());
        }

        return tutorProfileRepository.save(profile);
    }

    @Override
    public TutorProfile findByMentorEmail(String mentorEmail) {
        return tutorProfileRepository.findByUserEmail(mentorEmail)
                .orElseThrow(() -> new IllegalArgumentException("No tutor profile found for this account."));
    }

    @Override
    public TutorProfile findById(Long profileId) {
        return tutorProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Tutor profile not found."));
    }

    @Override
    public List<TutorProfile> findAll() {
        return tutorProfileRepository.findAll();
    }

    @Override
    public List<TutorProfile> search(Long subjectId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        boolean filterBySlot = dayOfWeek != null && startTime != null && endTime != null;

        List<TutorProfile> results = new ArrayList<>();
        for (TutorProfile profile : tutorProfileRepository.findAll()) {
            if (subjectId != null && profile.getSubjects().stream()
                    .noneMatch(ts -> ts.getSubject().getId().equals(subjectId))) {
                continue;
            }

            if (filterBySlot && profile.getAvailability().stream()
                    .noneMatch(a -> a.getDayOfWeek() == dayOfWeek
                            && !startTime.isBefore(a.getStartTime())
                            && !endTime.isAfter(a.getEndTime()))) {
                continue;
            }

            results.add(profile);
        }
        return results;
    }
}