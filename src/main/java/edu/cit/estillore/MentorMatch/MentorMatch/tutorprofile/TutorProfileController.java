package edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.estillore.MentorMatch.MentorMatch.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tutor-profiles")
@RequiredArgsConstructor
public class TutorProfileController {

    private final TutorProfileService tutorProfileService;
    private final ReviewService reviewService;

    /** Any authenticated role can browse tutor profiles (students searching, admins reviewing). */
    @GetMapping
    public ResponseEntity<List<TutorProfileResponse>> list() {
        List<TutorProfileResponse> profiles = tutorProfileService.findAll().stream()
                .map(this::toResponseWithRating)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profiles);
    }

    /**
     * Search tutors by subject and/or a requested weekly time slot (FR-004).
     * All params optional and combinable; dayOfWeek/startTime/endTime must be
     * supplied together to filter by availability. e.g.:
     * /api/tutor-profiles/search?subjectId=3&dayOfWeek=MONDAY&startTime=14:00&endTime=15:00
     */
    @GetMapping("/search")
    public ResponseEntity<List<TutorProfileResponse>> search(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) DayOfWeek dayOfWeek,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        List<TutorProfileResponse> profiles = tutorProfileService
                .search(subjectId, dayOfWeek, startTime, endTime).stream()
                .map(this::toResponseWithRating)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponseWithRating(tutorProfileService.findById(id)));
    }

    /** MENTOR-only, enforced in SecurityConfig. Returns the caller's own profile, if any. */
    @GetMapping("/me")
    public ResponseEntity<TutorProfileResponse> getOwnProfile(Authentication authentication) {
        return ResponseEntity.ok(toResponseWithRating(
                tutorProfileService.findByMentorEmail(authentication.getName())));
    }

    /** MENTOR-only, enforced in SecurityConfig. Creates the profile on first call, updates after. */
    @PutMapping("/me")
    public ResponseEntity<TutorProfileResponse> createOrUpdateOwnProfile(
            Authentication authentication,
            @Valid @RequestBody TutorProfileRequest request) {
        return ResponseEntity.ok(toResponseWithRating(
                tutorProfileService.createOrUpdateOwnProfile(authentication.getName(), request)));
    }

    /** FR-010: attaches the tutor's computed average rating + review count to every response. */
    private TutorProfileResponse toResponseWithRating(TutorProfile profile) {
        Double avg = reviewService.getAverageRating(profile.getId());
        long count = reviewService.getReviewCount(profile.getId());
        return TutorProfileResponse.fromEntity(profile, avg, count);
    }
}