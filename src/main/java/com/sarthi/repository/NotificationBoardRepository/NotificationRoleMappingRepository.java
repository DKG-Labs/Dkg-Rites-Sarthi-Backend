package com.sarthi.repository.NotificationBoardRepository;

import com.sarthi.entity.NotificationsBoard.NotificationRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRoleMappingRepository extends JpaRepository<NotificationRoleMapping,Long> {
    void deleteByNotificationId(Long notificationId);
}
