package com.sarthi.repository.NotificationBoardRepository;

import com.sarthi.entity.NotificationsBoard.NotificationAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationAttachmentRepository extends JpaRepository<NotificationAttachment, Long> {
}
