package edu.cit.estillore.MentorMatch.MentorMatch.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponseRequest {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
    private String studentNumber;
    private String program;
    private String expertise;
    private String department;

    public static UserResponseRequest fromEntity(User user) {
        return new UserResponseRequest(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStudentNumber(),
                user.getProgram(),
                user.getExpertise(),
                user.getDepartment()
        );
    }
}
