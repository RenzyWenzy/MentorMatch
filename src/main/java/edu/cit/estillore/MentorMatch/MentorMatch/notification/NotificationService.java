package edu.cit.estillore.MentorMatch.MentorMatch.notification;

import edu.cit.estillore.MentorMatch.MentorMatch.user.User;

import java.util.List;

public interface NotificationService {

    /** Creates a notification for the given recipient. Called internally by BookingService (FR-014). */
    Notification notify(User recipient, NotificationType type, String message, Long bookingId);

    List<Notification> findOwnNotifications(String email);

    long countUnread(String email);

    Notification markAsRead(String email, Long notificationId);

    void markAllAsRead(String email);
}