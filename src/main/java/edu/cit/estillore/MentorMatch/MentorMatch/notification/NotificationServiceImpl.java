package edu.cit.estillore.MentorMatch.MentorMatch.notification;

import edu.cit.estillore.MentorMatch.MentorMatch.user.User;
import edu.cit.estillore.MentorMatch.MentorMatch.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Notification notify(User recipient, NotificationType type, String message, Long bookingId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .bookingId(bookingId)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> findOwnNotifications(String email) {
        User user = findUser(email);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId());
    }

    @Override
    public long countUnread(String email) {
        User user = findUser(email);
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public Notification markAsRead(String email, Long notificationId) {
        User user = findUser(email);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new IllegalArgumentException("This notification does not belong to you.");
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        User user = findUser(email);

        List<Notification> unread = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .filter(n -> !n.isRead())
                .toList();

        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No account found for email: " + email));
    }
}