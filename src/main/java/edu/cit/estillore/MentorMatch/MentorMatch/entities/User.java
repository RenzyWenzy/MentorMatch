package edu.cit.estillore.MentorMatch.MentorMatch.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password; // stored as a BCrypt hash, never plain text

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    // Student-specific field (nullable for mentors)
    @Column(length = 50)
    private String studentNumber;

    // Student-specific field (nullable for mentors)
    @Column(length = 100)
    private String program;

    // Mentor-specific field (nullable for students)
    @Column(length = 100)
    private String expertise;

    // Mentor-specific field (nullable for students)
    @Column(length = 100)
    private String department;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
