package com.sarthi.service;

import com.sarthi.dto.NotificationBoardDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NotificationBoardService {

        NotificationDetailResponse createNotification(
                CreateNotificationRequest request,
                Long loggedInUserId);

        NotificationDetailResponse updateNotification(
                Long notificationId,
                UpdateNotificationRequest request,
                Long loggedInUserId);

        void publishNotification(
                Long notificationId,
                Long loggedInUserId);

        void archiveNotification(
                Long notificationId,
                Long loggedInUserId);

        void deleteNotification(
                Long notificationId,
                Long loggedInUserId);

      //  Page<NotificationListResponse> getAdminNotifications(NotificationSearchRequest request, Pageable pageable);

     //   NotificationDetailResponse getNotificationDetails(Long notificationId, Long userId);

        NotificationDetailResponse getNotificationDetails(
                Long notificationId,
                Long userId);
        public List<NotificationListResponse> getUserNotifications(
                String roleName);

        List<NotificationListResponse> getPopupNotifications(
                Long userId);

      //  void closePopupNotification(Long notificationId,Long userId);

     //   Long getUnreadNotificationCount(Long userId);

}
