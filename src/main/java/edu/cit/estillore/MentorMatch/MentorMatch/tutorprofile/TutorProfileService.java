package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface TutorProfileService {

    /** Creates the profile if the mentor doesn't have one yet, otherwise replaces subjects + bio. */
    TutorProfile createOrUpdateOwnProfile(String mentorEmail, TutorProfileRequest request);

    TutorProfile findByMentorEmail(String mentorEmail);

    /** Unrestricted lookup by id, for internal/admin use. Does not enforce BR-002 visibility. */
    TutorProfile findById(Long profileId);

    /**
     * BR-002: like findById, but a PENDING/REJECTED profile is only visible
     * to the mentor who owns it or to an ADMIN — everyone else gets a 404-style error.
     */
    TutorProfile findVisibleById(String requesterEmail, Long profileId);

    /** BR-002: only APPROVED profiles, i.e. what shows up when anyone browses the directory. */
    List<TutorProfile> findAll();

    /**
     * Filters tutors by subject and/or a requested weekly time slot (FR-004).
     * Any parameter left null is ignored. Passing all null returns every APPROVED profile (BR-002).
     */
    List<TutorProfile> search(Long subjectId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);

    /** ADMIN-only: profiles awaiting review (BR-002). */
    List<TutorProfile> findPending();

    /** ADMIN-only: approves a profile, making it visible in search. */
    TutorProfile approve(String adminEmail, Long profileId);

    /** ADMIN-only: rejects a profile with an optional reason; it stays hidden from search. */
    TutorProfile reject(String adminEmail, Long profileId, String reason);
}