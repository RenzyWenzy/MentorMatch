package edu.cit.estillore.MentorMatch.MentorMatch.user;

import edu.cit.estillore.MentorMatch.MentorMatch.auth.RegistrationRequest;

import java.util.List;

public interface UserService {

    /**
     * Validates and persists a new user account.
     * @throws IllegalArgumentException if the email is already registered
     *         or the passwords / role-specific fields are invalid.
     */
    User registerUser(RegistrationRequest request);

    boolean emailExists(String email);

    User findByEmail(String email);

    User findById(Long id);

    List<User> findAllUsers();

    /** FR-011: ADMIN reactivates a previously deactivated account. */
    User activateUser(String adminEmail, Long userId);

    /** FR-011: ADMIN suspends an account; the account should no longer be able to authenticate. */
    User deactivateUser(String adminEmail, Long userId);

    /**
     * FR-011: ADMIN permanently removes an account.
     * Fails with IllegalArgumentException if the account has related bookings,
     * reviews, or a tutor profile — deactivate it instead in that case.
     */
    void removeUser(String adminEmail, Long userId);
}