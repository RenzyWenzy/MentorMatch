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

    List<User> findAllUsers();
}
