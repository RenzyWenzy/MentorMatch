package edu.cit.estillore.MentorMatch.MentorMatch.service;

import edu.cit.estillore.MentorMatch.MentorMatch.dto.RegistrationRequest;
import edu.cit.estillore.MentorMatch.MentorMatch.entities.User;

public interface UserService {

    /**
     * Validates and persists a new user account.
     * @throws IllegalArgumentException if the email is already registered
     *         or the passwords / role-specific fields are invalid.
     */
    User registerUser(RegistrationRequest request);

    boolean emailExists(String email);

    User findByEmail(String email);

    java.util.List<User> findAllUsers();
}
