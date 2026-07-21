package edu.cit.estillore.MentorMatch.MentorMatch.user;

import edu.cit.estillore.MentorMatch.MentorMatch.auth.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(RegistrationRequest request) {

        if (request.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be self-registered.");
        }

        if (emailExists(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        if (request.getRole() == Role.STUDENT
                && (isBlank(request.getStudentNumber()) || isBlank(request.getProgram()))) {
            throw new IllegalArgumentException("Student number and program are required for student accounts.");
        }

        if (request.getRole() == Role.MENTOR
                && (isBlank(request.getExpertise()) || isBlank(request.getDepartment()))) {
            throw new IllegalArgumentException("Expertise and department are required for mentor accounts.");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .studentNumber(request.getRole() == Role.STUDENT ? request.getStudentNumber() : null)
                .program(request.getRole() == Role.STUDENT ? request.getProgram() : null)
                .expertise(request.getRole() == Role.MENTOR ? request.getExpertise() : null)
                .department(request.getRole() == Role.MENTOR ? request.getDepartment() : null)
                .build();

        return userRepository.save(user);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("No account found for email: " + email));
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
