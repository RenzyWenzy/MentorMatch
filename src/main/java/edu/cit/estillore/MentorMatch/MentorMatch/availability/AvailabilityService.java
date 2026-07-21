package edu.cit.estillore.MentorMatch.MentorMatch.availability;

import java.util.List;

public interface AvailabilityService {

    /** Replaces the mentor's entire weekly availability with the given slots. */
    List<Availability> replaceOwnAvailability(String mentorEmail, AvailabilityUpdateRequest request);

    List<Availability> findByTutorProfileId(Long tutorProfileId);
}
