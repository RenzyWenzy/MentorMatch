package edu.cit.estillore.MentorMatch.MentorMatch.service;

import edu.cit.estillore.MentorMatch.MentorMatch.dto.AvailabilityUpdateRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.entities.Availability;

import java.util.List;

public interface AvailabilityService {

    /** Replaces the mentor's entire weekly availability with the given slots. */
    List<Availability> replaceOwnAvailability(String mentorEmail, AvailabilityUpdateRequest request);

    List<Availability> findByTutorProfileId(Long tutorProfileId);
}
