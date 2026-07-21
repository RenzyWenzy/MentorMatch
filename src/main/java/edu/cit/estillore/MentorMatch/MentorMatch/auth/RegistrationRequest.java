package edu.cit.estillore.MentorMatch.MentorMatch.auth;

import edu.cit.estillore.MentorMatch.MentorMatch.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Backing object for the registration form.
 * Kept separate from the User entity so raw passwords / form quirks
 * never leak into persistence code.
 */
@Data
public class RegistrationRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @NotNull(message = "Please select an account type")
    private Role role;

    // Student-only (validated conditionally in the service layer)
    private String studentNumber;
    private String program;

    // Mentor-only (validated conditionally in the service layer)
    private String expertise;
    private String department;
}
