package edu.cit.estillore.MentorMatch.MentorMatch.controller;

import edu.cit.estillore.MentorMatch.MentorMatch.dto.TutorProfileRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.dto.TutorProfileResponse;
import edu.cit.estillore.MentorMatch.MentorMatch.service.TutorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tutor-profiles")
@RequiredArgsConstructor
public class TutorProfileController {

    private final TutorProfileService tutorProfileService;

    /** Any authenticated role can browse tutor profiles (students searching, admins reviewing). */
    @GetMapping
    public ResponseEntity<List<TutorProfileResponse>> list() {
        List<TutorProfileResponse> profiles = tutorProfileService.findAll().stream()
                .map(TutorProfileResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(TutorProfileResponse.fromEntity(tutorProfileService.findById(id)));
    }

    /** MENTOR-only, enforced in SecurityConfig. Returns the caller's own profile, if any. */
    @GetMapping("/me")
    public ResponseEntity<TutorProfileResponse> getOwnProfile(Authentication authentication) {
        return ResponseEntity.ok(TutorProfileResponse.fromEntity(
                tutorProfileService.findByMentorEmail(authentication.getName())));
    }

    /** MENTOR-only, enforced in SecurityConfig. Creates the profile on first call, updates after. */
    @PutMapping("/me")
    public ResponseEntity<TutorProfileResponse> createOrUpdateOwnProfile(
            Authentication authentication,
            @Valid @RequestBody TutorProfileRequest request) {
        return ResponseEntity.ok(TutorProfileResponse.fromEntity(
                tutorProfileService.createOrUpdateOwnProfile(authentication.getName(), request)));
    }
}
