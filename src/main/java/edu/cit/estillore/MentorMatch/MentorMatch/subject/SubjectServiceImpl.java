package edu.cit.estillore.MentorMatch.MentorMatch.subject;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.estillore.MentorMatch.MentorMatch.booking.BookingRepository;
import edu.cit.estillore.MentorMatch.MentorMatch.tutorprofile.TutorSubjectRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final TutorSubjectRepository tutorSubjectRepository;
    private final BookingRepository bookingRepository;

    @Override
    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    @Override
    @Transactional
    public Subject create(SubjectRequest request) {
        if (subjectRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new IllegalArgumentException("A subject with this name already exists.");
        }
        Subject subject = Subject.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .build();
        return subjectRepository.save(subject);
    }

    @Override
    @Transactional
    public Subject update(Long id, SubjectRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found."));

        String newName = request.getName().trim();
        if (!subject.getName().equalsIgnoreCase(newName) && subjectRepository.existsByNameIgnoreCase(newName)) {
            throw new IllegalArgumentException("A subject with this name already exists.");
        }

        subject.setName(newName);
        subject.setDescription(request.getDescription());
        return subjectRepository.save(subject);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new IllegalArgumentException("Subject not found.");
        }

        // Guard against FK violations: without this, deleting an in-use subject
        // throws an unhandled DataIntegrityViolationException (raw 500) instead
        // of the clean validation error every other endpoint in this app returns.
        boolean inUseByTutors = !tutorSubjectRepository.findBySubjectId(id).isEmpty();
        if (inUseByTutors) {
            throw new IllegalArgumentException(
                    "This subject is still assigned to one or more tutors and can't be removed.");
        }

        boolean inUseByBookings = bookingRepository.existsBySubjectId(id);
        if (inUseByBookings) {
            throw new IllegalArgumentException(
                    "This subject has existing booking history and can't be removed.");
        }

        subjectRepository.deleteById(id);
    }
}