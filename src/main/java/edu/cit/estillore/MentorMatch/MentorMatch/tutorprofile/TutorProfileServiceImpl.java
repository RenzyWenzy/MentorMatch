package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.estillore.MentorMatch.MentorMatch.notification.NotificationService;
import edu.cit.estillore.MentorMatch.MentorMatch.notification.NotificationType;
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
    private final NotificationService notificationService;

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

        // BR-002: an edit to a PENDING or REJECTED profile is a resubmission, so it
        // goes (back) into the review queue. An already-APPROVED profile stays
        // approved through minor edits — only a fresh/rejected profile needs review.
        if (profile.getApprovalStatus() != ApprovalStatus.APPROVED) {
            profile.setApprovalStatus(ApprovalStatus.PENDING);
            profile.setRejectionReason(null);
        }

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
    public TutorProfile findVisibleById(String requesterEmail, Long profileId) {
        TutorProfile profile = findById(profileId);

        if (profile.getApprovalStatus() == ApprovalStatus.APPROVED) {
            return profile;
        }

        // Not yet approved (or rejected): only the owning mentor or an ADMIN may view it.
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found for email: " + requesterEmail));

        boolean isOwner = profile.getUser().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("Tutor profile not found.");
        }

        return profile;
    }

    @Override
    public List<TutorProfile> findAll() {
        // BR-002: only approved profiles are visible to the general directory.
        return tutorProfileRepository.findAll().stream()
                .filter(p -> p.getApprovalStatus() == ApprovalStatus.APPROVED)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<TutorProfile> search(Long subjectId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        boolean filterBySlot = dayOfWeek != null && startTime != null && endTime != null;

        List<TutorProfile> results = new ArrayList<>();
        for (TutorProfile profile : tutorProfileRepository.findAll()) {
            // BR-002: unapproved profiles never appear in search results.
            if (profile.getApprovalStatus() != ApprovalStatus.APPROVED) {
                continue;
            }

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

    @Override
    public List<TutorProfile> findPending() {
        return tutorProfileRepository.findAll().stream()
                .filter(p -> p.getApprovalStatus() == ApprovalStatus.PENDING)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public TutorProfile approve(String adminEmail, Long profileId) {
        // adminEmail isn't needed to authorize this (SecurityConfig already restricts the
        // route to ADMIN), but kept in the signature for future auditing, same as User's admin actions.
        TutorProfile profile = findById(profileId);
        profile.setApprovalStatus(ApprovalStatus.APPROVED);
        profile.setRejectionReason(null);
        TutorProfile saved = tutorProfileRepository.save(profile);

        notificationService.notify(
                saved.getUser(),
                NotificationType.PROFILE_APPROVED,
                "Your tutor profile has been approved and is now visible to students.",
                null);

        return saved;
    }

    @Override
    @Transactional
    public TutorProfile reject(String adminEmail, Long profileId, String reason) {
        TutorProfile profile = findById(profileId);
        profile.setApprovalStatus(ApprovalStatus.REJECTED);
        profile.setRejectionReason(reason);
        TutorProfile saved = tutorProfileRepository.save(profile);

        String message = (reason == null || reason.isBlank())
                ? "Your tutor profile was not approved. Please review and update it."
                : "Your tutor profile was not approved: " + reason;
        notificationService.notify(saved.getUser(), NotificationType.PROFILE_REJECTED, message, null);

        return saved;
    }
}