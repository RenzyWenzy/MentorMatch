package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public interface TutorProfileService {

    /** Creates the profile if the mentor doesn't have one yet, otherwise replaces subjects + bio. */
    TutorProfile createOrUpdateOwnProfile(String mentorEmail, TutorProfileRequest request);

    TutorProfile findByMentorEmail(String mentorEmail);

    TutorProfile findById(Long profileId);

    List<TutorProfile> findAll();

    /**
     * Filters tutors by subject and/or a requested weekly time slot (FR-004).
     * Any parameter left null is ignored. Passing all null is equivalent to findAll().
     */
    List<TutorProfile> search(Long subjectId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime);
}