package com.sarthi.repository.NotificationBoardRepository;

import com.sarthi.entity.NotificationsBoard.NotificationAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationAuditLogRepository extends JpaRepository<NotificationAuditLog, Long> {

}
