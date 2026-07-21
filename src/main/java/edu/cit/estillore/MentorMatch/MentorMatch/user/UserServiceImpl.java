package edu.cit.estillore.MentorMatch.MentorMatch.user;

import edu.cit.estillore.MentorMatch.MentorMatch.auth.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
                .active(true)
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
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No account found for id: " + id));
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User activateUser(String adminEmail, Long userId) {
        // adminEmail isn't needed to authorize this one (no self-lockout risk),
        // but kept in the signature for symmetry with deactivate/remove and future auditing.
        User target = findById(userId);
        target.setActive(true);
        return userRepository.save(target);
    }

    @Override
    @Transactional
    public User deactivateUser(String adminEmail, Long userId) {
        User admin = findByEmail(adminEmail);
        User target = findById(userId);

        if (admin.getId().equals(target.getId())) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }

        target.setActive(false);
        return userRepository.save(target);
    }

    @Override
    @Transactional
    public void removeUser(String adminEmail, Long userId) {
        User admin = findByEmail(adminEmail);
        User target = findById(userId);

        if (admin.getId().equals(target.getId())) {
            throw new IllegalArgumentException("You cannot remove your own account.");
        }

        try {
            userRepository.delete(target);
            userRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "This account has existing bookings, reviews, or a tutor profile and can't be removed. "
                            + "Deactivate it instead.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}