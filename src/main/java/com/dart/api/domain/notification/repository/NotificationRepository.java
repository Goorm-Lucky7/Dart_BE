package com.dart.api.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dart.api.domain.notification.entity.Notification;
import com.dart.api.domain.notification.entity.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	boolean existsByNotificationType(NotificationType notificationType);
}
