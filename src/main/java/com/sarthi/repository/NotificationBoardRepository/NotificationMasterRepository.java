package com.sarthi.repository.NotificationBoardRepository;

import com.sarthi.entity.NotificationsBoard.NotificationMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface NotificationMasterRepository
        extends JpaRepository<NotificationMaster, Long> {

    @Query("""
    SELECT DISTINCT n
    FROM NotificationMaster n
    JOIN n.roleMappings rm
    WHERE rm.role.roleId IN :roleIds
    AND n.status = com.sarthi.entity.NotificationsBoard.NotificationStatus.PUBLISHED
    AND n.isDeleted = false
    ORDER BY n.createdDate DESC
    """)
    Page<NotificationMaster> findNotificationsByRoles(
            @Param("roleIds") List<Long> roleIds,
            Pageable pageable);

  /*  @Query("""
    SELECT DISTINCT n
    FROM NotificationMaster n
    JOIN n.roleMappings rm
    WHERE rm.role.roleId IN :roleIds
    AND n.popupNotification = true
    AND n.status = com.sarthi.entity.NotificationsBoard.NotificationStatus.PUBLISHED
    AND n.isDeleted = false
    ORDER BY n.createdDate DESC
    """)
    Page<NotificationMaster> findPopupNotifications(
            @Param("roleIds") List<Long> roleIds); */
  @Query("""
SELECT DISTINCT n
FROM NotificationMaster n
JOIN n.roleMappings rm
JOIN rm.role r
WHERE r.roleName = :roleName
AND n.status = com.sarthi.entity.NotificationsBoard.NotificationStatus.PUBLISHED
AND n.isDeleted = false
ORDER BY n.createdDate DESC
""")
  List<NotificationMaster> findNotificationsByRoleName(
          @Param("roleName") String roleName);

    @Query("""
    SELECT DISTINCT n
    FROM NotificationMaster n
    JOIN n.roleMappings rm
    WHERE rm.role.roleId IN :roleIds
    AND n.popupNotification = true
    AND n.status = com.sarthi.entity.NotificationsBoard.NotificationStatus.PUBLISHED
    AND n.isDeleted = false
    ORDER BY n.createdDate DESC
    """)
    List<NotificationMaster> findPopupNotification(
            @Param("roleIds") List<Long> roleIds);

    @Query("""
    SELECT COUNT(n)
    FROM NotificationMaster n
    WHERE FUNCTION('DATE', n.createdDate) = :today
    """)
    Long countTodayNotifications(
            @Param("today") LocalDate today);
}
