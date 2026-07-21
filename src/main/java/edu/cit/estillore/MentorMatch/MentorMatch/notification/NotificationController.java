package edu.cit.estillore.MentorMatch.MentorMatch.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Any authenticated role — every user (student, mentor, admin) gets their own notifications. */
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>> ownNotifications(Authentication authentication) {
        List<NotificationResponse> notifications = notificationService
                .findOwnNotifications(authentication.getName()).stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(authentication.getName())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(Authentication authentication, @PathVariable Long id) {
        Notification notification = notificationService.markAsRead(authentication.getName(), id);
        return ResponseEntity.ok(NotificationResponse.fromEntity(notification));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}