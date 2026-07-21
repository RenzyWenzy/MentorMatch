package edu.cit.estillore.MentorMatch.MentorMatch.subject;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /** Open to any authenticated role — students/mentors need this list too (search, profile setup). */
    @GetMapping
    public ResponseEntity<List<SubjectResponse>> list() {
        List<SubjectResponse> subjects = subjectService.findAll().stream()
                .map(SubjectResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjects);
    }

    /** ADMIN-only, enforced in SecurityConfig (FR-012). */
    @PostMapping
    public ResponseEntity<SubjectResponse> create(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(SubjectResponse.fromEntity(subjectService.create(request)));
    }

    /** ADMIN-only, enforced in SecurityConfig. */
    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponse> update(@PathVariable Long id, @Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.ok(SubjectResponse.fromEntity(subjectService.update(id, request)));
    }

    /** ADMIN-only, enforced in SecurityConfig. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        subjectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
