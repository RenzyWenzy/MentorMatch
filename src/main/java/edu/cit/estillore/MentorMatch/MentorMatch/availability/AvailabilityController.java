package edu.cit.estillore.MentorMatch.MentorMatch.availability;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    /** MENTOR-only, enforced in SecurityConfig. Replaces the caller's entire weekly schedule. */
    @PutMapping("/me")
    public ResponseEntity<List<AvailabilitySlotResponse>> replaceOwnAvailability(
            Authentication authentication,
            @Valid @RequestBody AvailabilityUpdateRequest request) {
        List<AvailabilitySlotResponse> slots = availabilityService
                .replaceOwnAvailability(authentication.getName(), request).stream()
                .map(AvailabilitySlotResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(slots);
    }

    /** Any authenticated role — students need this to see a tutor's open slots before booking. */
    @GetMapping("/tutor-profile/{tutorProfileId}")
    public ResponseEntity<List<AvailabilitySlotResponse>> getByTutorProfile(@PathVariable Long tutorProfileId) {
        List<AvailabilitySlotResponse> slots = availabilityService.findByTutorProfileId(tutorProfileId).stream()
                .map(AvailabilitySlotResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(slots);
    }
}
