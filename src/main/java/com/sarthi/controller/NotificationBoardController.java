package com.sarthi.controller;


import com.sarthi.dto.NotificationBoardDtos.CreateNotificationRequest;
import com.sarthi.dto.NotificationBoardDtos.NotificationDetailResponse;
import com.sarthi.dto.NotificationBoardDtos.NotificationListResponse;
import com.sarthi.dto.NotificationBoardDtos.UpdateNotificationRequest;
import com.sarthi.service.NotificationBoardService;
import com.sarthi.util.ResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

    @RestController
    @RequestMapping("/api/notification-board")
    @RequiredArgsConstructor
    public class NotificationBoardController {

        private final NotificationBoardService notificationBoardService;

        @PostMapping
        public ResponseEntity<Object> createNotification(
                @RequestBody CreateNotificationRequest request,
                @RequestParam Long userId) {

            NotificationDetailResponse response =
                    notificationBoardService.createNotification(
                            request,
                            userId);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(response),
                    HttpStatus.OK);
        }

        @PutMapping("/{notificationId}")
        public ResponseEntity<Object> updateNotification(
                @PathVariable Long notificationId,
                @RequestBody UpdateNotificationRequest request,
                @RequestParam Long userId) {

            NotificationDetailResponse response =
                    notificationBoardService.updateNotification(
                            notificationId,
                            request,
                            userId);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(response),
                    HttpStatus.OK);
        }

        @PutMapping("/{notificationId}/publish")
        public ResponseEntity<Object> publishNotification(
                @PathVariable Long notificationId,
                @RequestParam Long userId) {

            notificationBoardService.publishNotification(
                    notificationId,
                    userId);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            "Notification Published Successfully"),
                    HttpStatus.OK);
        }

        @PutMapping("/{notificationId}/archive")
        public ResponseEntity<Object> archiveNotification(
                @PathVariable Long notificationId,
                @RequestParam Long userId) {

            notificationBoardService.archiveNotification(
                    notificationId,
                    userId);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            "Notification Archived Successfully"),
                    HttpStatus.OK);
        }

        @DeleteMapping("/{notificationId}")
        public ResponseEntity<Object> deleteNotification(
                @PathVariable Long notificationId,
                @RequestParam Long userId) {

            notificationBoardService.deleteNotification(
                    notificationId,
                    userId);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            "Notification Deleted Successfully"),
                    HttpStatus.OK);
        }

        @GetMapping("/user")
        public ResponseEntity<Object> getUserNotifications(
                @RequestParam String roleName) {

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(
                            notificationBoardService
                                    .getUserNotifications(roleName)),
                    HttpStatus.OK);
        }

        @GetMapping("/popup")
        public ResponseEntity<Object> getPopupNotifications(
                @RequestParam Long userId) {

            List<NotificationListResponse> response =
                    notificationBoardService.getPopupNotifications(
                            userId);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(response),
                    HttpStatus.OK);
        }

        @GetMapping("/{notificationId}")
        public ResponseEntity<Object> getNotificationDetails(
                @PathVariable Long notificationId,
                @RequestParam Long userId) {

            NotificationDetailResponse response =
                    notificationBoardService
                            .getNotificationDetails(
                                    notificationId,
                                    userId);

            return new ResponseEntity<>(
                    ResponseBuilder.getSuccessResponse(response),
                    HttpStatus.OK);
        }

}
