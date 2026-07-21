package edu.cit.estillore.MentorMatch.MentorMatch.service;

import edu.cit.estillore.MentorMatch.MentorMatch.dto.TutorProfileRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.dto.TutorSubjectRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.entities.*;
import edu.cit.estillore.MentorMatch.MentorMatch.repository.SubjectRepository;
import edu.cit.estillore.MentorMatch.MentorMatch.repository.TutorProfileRepository;
import edu.cit.estillore.MentorMatch.MentorMatch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
}
