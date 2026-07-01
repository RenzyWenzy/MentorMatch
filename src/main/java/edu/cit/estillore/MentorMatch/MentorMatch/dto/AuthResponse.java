package edu.cit.estillore.MentorMatch.MentorMatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserResponseRequest user;
}
