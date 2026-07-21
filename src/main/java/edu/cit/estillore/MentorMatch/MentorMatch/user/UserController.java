package edu.cit.estillore.MentorMatch.MentorMatch.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Returns the currently authenticated user's profile. Any logged-in role can call this. */
    @GetMapping("/me")
    public ResponseEntity<UserResponseRequest> me(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(UserResponseRequest.fromEntity(user));
    }

    /** STUDENT-only route, enforced in SecurityConfig. */
    @GetMapping("/dashboard/student")
    public ResponseEntity<UserResponseRequest> studentDashboard(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(UserResponseRequest.fromEntity(user));
    }

    /** MENTOR-only route, enforced in SecurityConfig. */
    @GetMapping("/dashboard/mentor")
    public ResponseEntity<UserResponseRequest> mentorDashboard(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(UserResponseRequest.fromEntity(user));
    }

    /** ADMIN-only route, enforced in SecurityConfig. */
    @GetMapping("/dashboard/admin")
    public ResponseEntity<UserResponseRequest> adminDashboard(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return ResponseEntity.ok(UserResponseRequest.fromEntity(user));
    }

    /** ADMIN-only: list all registered users (for the account-management screen). */
    @GetMapping
    public ResponseEntity<List<UserResponseRequest>> listUsers() {
        List<UserResponseRequest> users = userService.findAllUsers()
                .stream()
                .map(UserResponseRequest::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
