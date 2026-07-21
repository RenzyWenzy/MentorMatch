package edu.cit.estillore.MentorMatch.MentorMatch.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String message;
    private Long bookingId;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.getBookingId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}