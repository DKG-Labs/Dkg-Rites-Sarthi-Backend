package com.sarthi.repository.NotificationBoardRepository;

import com.sarthi.entity.NotificationsBoard.NotificationViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationViewLogRepository extends JpaRepository<NotificationViewLog, Long> {

    boolean existsByNotificationIdAndUserUserId(
            Long notificationId,
            Integer userId);

    Optional<NotificationViewLog>
    findByNotificationIdAndUserUserId(
            Long notificationId,
            Integer userId);
}
