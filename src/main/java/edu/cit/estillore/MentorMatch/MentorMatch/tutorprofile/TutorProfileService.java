package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import java.util.List;

public interface TutorProfileService {

    /** Creates the profile if the mentor doesn't have one yet, otherwise replaces subjects + bio. */
    TutorProfile createOrUpdateOwnProfile(String mentorEmail, TutorProfileRequest request);

    TutorProfile findByMentorEmail(String mentorEmail);

    TutorProfile findById(Long profileId);

    List<TutorProfile> findAll();
}
