package edu.cit.estillore.MentorMatch.MentorMatch.service;

import edu.cit.estillore.MentorMatch.MentorMatch.dto.AvailabilitySlotRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.dto.AvailabilityUpdateRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.entities.Availability;
import edu.cit.estillore.MentorMatch.MentorMatch.entities.TutorProfile;
import edu.cit.estillore.MentorMatch.MentorMatch.repository.AvailabilityRepository;
import edu.cit.estillore.MentorMatch.MentorMatch.repository.TutorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final TutorProfileRepository tutorProfileRepository;

    @Override
    @Transactional
    public List<Availability> replaceOwnAvailability(String mentorEmail, AvailabilityUpdateRequest request) {
        TutorProfile profile = tutorProfileRepository.findByUserEmail(mentorEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Create your tutor profile before setting availability."));

        validateNoOverlaps(request.getSlots());

        profile.getAvailability().clear();
        List<Availability> slots = new ArrayList<>();
        for (AvailabilitySlotRequest s : request.getSlots()) {
            if (!s.getStartTime().isBefore(s.getEndTime())) {
                throw new IllegalArgumentException("Each slot's start time must be before its end time.");
            }
            slots.add(Availability.builder()
                    .tutorProfile(profile)
                    .dayOfWeek(s.getDayOfWeek())
                    .startTime(s.getStartTime())
                    .endTime(s.getEndTime())
                    .build());
        }
        profile.getAvailability().addAll(slots);

        tutorProfileRepository.save(profile);
        return profile.getAvailability();
    }

    @Override
    public List<Availability> findByTutorProfileId(Long tutorProfileId) {
        return availabilityRepository.findByTutorProfileId(tutorProfileId);
    }

    /** Rejects overlapping slots on the same day within the submitted batch. */
    private void validateNoOverlaps(List<AvailabilitySlotRequest> slots) {
        for (int i = 0; i < slots.size(); i++) {
            for (int j = i + 1; j < slots.size(); j++) {
                AvailabilitySlotRequest a = slots.get(i);
                AvailabilitySlotRequest b = slots.get(j);
                if (a.getDayOfWeek() != b.getDayOfWeek()) continue;
                boolean overlaps = a.getStartTime().isBefore(b.getEndTime())
                        && b.getStartTime().isBefore(a.getEndTime());
                if (overlaps) {
                    throw new IllegalArgumentException(
                            "Availability slots on " + a.getDayOfWeek() + " overlap.");
                }
            }
        }
    }
}
