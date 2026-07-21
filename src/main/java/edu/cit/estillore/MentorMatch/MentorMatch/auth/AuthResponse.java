package edu.cit.estillore.MentorMatch.MentorMatch.auth;

import edu.cit.estillore.MentorMatch.MentorMatch.user.UserResponseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserResponseRequest user;
}
