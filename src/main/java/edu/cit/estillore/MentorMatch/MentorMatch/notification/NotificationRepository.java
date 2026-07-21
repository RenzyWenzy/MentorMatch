package edu.cit.estillore.MentorMatch.MentorMatch.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);
}