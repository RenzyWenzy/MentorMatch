package edu.cit.estillore.MentorMatch.MentorMatch.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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

    /** ADMIN-only, enforced in SecurityConfig (FR-011). */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseRequest> getById(@PathVariable Long id) {
        return ResponseEntity.ok(UserResponseRequest.fromEntity(userService.findById(id)));
    }

    /** ADMIN-only, enforced in SecurityConfig (FR-011). Reactivates a suspended account. */
    @PutMapping("/{id}/activate")
    public ResponseEntity<UserResponseRequest> activate(Authentication authentication, @PathVariable Long id) {
        User user = userService.activateUser(authentication.getName(), id);
        return ResponseEntity.ok(UserResponseRequest.fromEntity(user));
    }

    /** ADMIN-only, enforced in SecurityConfig (FR-011). Suspends an account. */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<UserResponseRequest> deactivate(Authentication authentication, @PathVariable Long id) {
        User user = userService.deactivateUser(authentication.getName(), id);
        return ResponseEntity.ok(UserResponseRequest.fromEntity(user));
    }

    /** ADMIN-only, enforced in SecurityConfig (FR-011). Permanently removes an account. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(Authentication authentication, @PathVariable Long id) {
        userService.removeUser(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}