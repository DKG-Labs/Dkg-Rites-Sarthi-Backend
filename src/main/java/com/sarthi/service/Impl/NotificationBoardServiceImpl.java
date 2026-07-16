package com.sarthi.service.Impl;

import com.azure.core.exception.ResourceNotFoundException;
import com.sarthi.constant.AppConstant;
import com.sarthi.dto.NotificationBoardDtos.*;
import com.sarthi.entity.NotificationsBoard.*;
import com.sarthi.entity.RoleMaster;
import com.sarthi.entity.UserMaster;
import com.sarthi.entity.UserRoleMaster;
import com.sarthi.exception.BusinessException;
import com.sarthi.exception.ErrorDetails;
import com.sarthi.repository.NotificationBoardRepository.*;
import com.sarthi.repository.RoleMasterRepository;
import com.sarthi.repository.UserMasterRepository;
import com.sarthi.repository.UserRoleMasterRepository;
import com.sarthi.service.NotificationBoardService;
import com.sarthi.util.NotificationNumberGenerator;
import com.sarthi.util.NotificationService;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationBoardServiceImpl implements NotificationBoardService {

    @Autowired
    private UserMasterRepository userMasterRepository;
    @Autowired
    private UserRoleMasterRepository userRoleMasterRepository;
    @Autowired
    private RoleMasterRepository roleMasterRepository;
    @Autowired
    private NotificationMasterRepository notificationMasterRepository;
    @Autowired
    private NotificationAttachmentRepository notificationAttachmentRepository;
    @Autowired
    private NotificationAuditLogRepository notificationAuditLogRepository;
    @Autowired
    private NotificationRoleMappingRepository notificationRoleMappingRepository;
    @Autowired
    private NotificationNumberGenerator notificationNumberGenerator;
    @Autowired
    private NotificationViewLogRepository notificationViewLogRepository;
    @Transactional
    @Override
    public NotificationDetailResponse createNotification(
            CreateNotificationRequest request,
            Long userId) {

        validateNotification(request);

        UserMaster user = getUser(userId);

        NotificationMaster notification =
                new NotificationMaster();

        notification.setNotificationNumber(
                notificationNumberGenerator.generateNotificationNumber());

        notification.setTitle(
                request.getTitle());

        notification.setContent(
                request.getContent());

        notification.setEffectiveFrom(
                request.getEffectiveFrom());

        notification.setEffectiveTill(
                request.getEffectiveTill());

        notification.setPopupNotification(
                Boolean.TRUE.equals(
                        request.getPopupNotification()));

        notification.setIssuingAuthority(
                request.getIssuingAuthority());

        if(request.getStatus().equalsIgnoreCase("PUBLISHED")){
            notification.setStatus(
                    NotificationStatus.PUBLISHED);
        }else{
            notification.setStatus(NotificationStatus.DRAFT);
        }


        notification.setCreatedBy(
                user.getUsername());

        notification.setCreatedDate(
                LocalDateTime.now());

        notification =
                notificationMasterRepository.save(notification);

        saveRoleMappings(
                notification,
                request.getRoleIds(),
                user);

        createAuditLog(
                notification.getId(),
                "CREATE",
                user.getUsername(),
                "Notification Created");

        return mapToDetailResponse(notification);
    }

    @Transactional
    @Override
    public NotificationDetailResponse updateNotification(
            Long notificationId,
            UpdateNotificationRequest request,
            Long userId) {

        NotificationMaster notification =
                getNotification(notificationId);

        if(notification.getStatus()
                != NotificationStatus.DRAFT){

            throw new ValidationException(
                    "Only Draft notifications can be modified");
        }

        UserMaster user = getUser(userId);

        notification.setTitle(
                request.getTitle());

        notification.setContent(
                request.getContent());

        notification.setEffectiveFrom(
                request.getEffectiveFrom());

        notification.setEffectiveTill(
                request.getEffectiveTill());

        notification.setPopupNotification(
                request.getPopupNotification());

        notification.setIssuingAuthority(
                request.getIssuingAuthority());

        notification.setUpdatedBy(
                user.getUsername());

        notification.setUpdatedDate(
                LocalDateTime.now());

        notificationMasterRepository.save(notification);
        notificationRoleMappingRepository
                .deleteByNotificationId(notificationId);

        saveRoleMappings(
                notification,
                request.getRoleIds(),
                user);

        createAuditLog(
                notificationId,
                "UPDATE",
                user.getUsername(),
                "Notification Updated");

        return mapToDetailResponse(notification);
    }

    @Transactional
    @Override
    public void archiveNotification(
            Long notificationId,
            Long userId) {

        NotificationMaster notification =
                getNotification(notificationId);

        UserMaster user =
                getUser(userId);

        notification.setStatus(
                NotificationStatus.ARCHIVED);

        notification.setUpdatedBy(
                user.getUsername());

        notification.setUpdatedDate(
                LocalDateTime.now());

        notificationMasterRepository.save(notification);

        createAuditLog(
                notificationId,
                "ARCHIVE",
                user.getUsername(),
                "Notification Archived");
    }

    @Transactional
    @Override
    public void deleteNotification(
            Long notificationId,
            Long userId) {

        NotificationMaster notification =
                getNotification(notificationId);

        UserMaster user =
                getUser(userId);

        notification.setIsDeleted(true);

        notification.setUpdatedBy(
                user.getUsername());

        notification.setUpdatedDate(
                LocalDateTime.now());

        notificationMasterRepository.save(notification);

     createAuditLog(
                notificationId,
                "DELETE",
                user.getUsername(),
                "Soft Deleted");
    }

    @Override
    public List<NotificationListResponse> getUserNotifications(
            String roleName) {

        List<NotificationMaster> notifications =
                notificationMasterRepository
                        .findNotificationsByRoleName(roleName);

        return notifications.stream()
                .map(this::mapToListResponse)
                .toList();
    }

  /*  @Transactional
    @Override
    public NotificationDetailResponse getNotificationDetails(
            Long notificationId,
            Long userId) {

        NotificationMaster notification =
                getNotification(notificationId);

        saveViewLog(
                notification,
                userId);

        return mapToDetailResponse(
                notification);
    }*/

 /*   @Transactional
    public void saveViewLog(
            NotificationMaster notification,
            Long userId) {

        boolean exists =
                viewLogRepository
                        .existsByNotificationIdAndUserId(
                                notification.getId(),
                                userId);

        if(exists){
            return;
        }

        UserMaster user =
                getUser(userId);

        NotificationViewLog log =
                new NotificationViewLog();

        log.setNotification(
                notification);

        log.setUser(
                user);

        log.setViewedAt(
                LocalDateTime.now());

        log.setCreatedBy(
                user.getUsername());

        log.setCreatedDate(
                LocalDateTime.now());

        viewLogRepository.save(log);
    }*/

    @Override
    public List<NotificationListResponse>
    getPopupNotifications(Long userId) {

        List<Long> roleIds =
                userRoleMasterRepository
                        .findRoleIdsByUserId(userId);

        return notificationMasterRepository
                .findPopupNotification(roleIds)
                .stream()
                .map(this::mapToListResponse)
                .toList();
    }

 /*   @Transactional
    @Override
    public void closePopupNotification(
            Long notificationId,
            Long userId) {

        NotificationViewLog log =
                viewLogRepository
                        .findByNotificationIdAndUserId(
                                notificationId,
                                userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "View log not found"));

        log.setPopupClosed(true);

        log.setPopupClosedAt(
                LocalDateTime.now());

        viewLogRepository.save(log);
    }

      @Override
    public Long getUnreadNotificationCount(
            Long userId) {

        List<Long> roleIds =
                userRoleMasterRepository
                        .findRoleIdsByUserId(userId);

        Long totalNotifications =
                notificationMasterRepository
                        .countPublishedNotifications(
                                roleIds);

        Long viewedNotifications =
                viewLogRepository
                        .countViewedNotifications(
                                userId);

        return totalNotifications
                - viewedNotifications;
    }*/

    private UserMaster getUser(Long userId) {

        return userMasterRepository.findByUserId(userId.intValue())
                .orElseThrow(() ->
                        new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "User not found")));
    }

    @Transactional
    @Override
    public void publishNotification(
            Long notificationId,
            Long userId) {

        NotificationMaster notification =
                getNotification(notificationId);

        UserMaster user =
                getUser(userId);

        if (!NotificationStatus.DRAFT.equals(notification.getStatus())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Only Draft notifications can be published"));
        }

        notification.setStatus(NotificationStatus.PUBLISHED);
        notification.setUpdatedBy(user.getUsername());
        notification.setUpdatedDate(LocalDateTime.now());

        notificationMasterRepository.save(notification);

        createAuditLog(
                notification.getId(),
                "PUBLISH",
                user.getUsername(),
                "Notification Published");
    }


    private NotificationMaster getNotification(
            Long notificationId) {

        return notificationMasterRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new BusinessException(
                                new ErrorDetails(
                                        AppConstant.ERROR_CODE_RESOURCE,
                                        AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                        AppConstant.ERROR_TYPE_VALIDATION,
                                        "Notification not found")));
    }


    private void validateNotification(
            CreateNotificationRequest request) {

        if (request.getEffectiveFrom() == null) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Effective From Date is mandatory"));
        }

        if (request.getEffectiveTill() != null &&
                request.getEffectiveTill()
                        .isBefore(request.getEffectiveFrom())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "Effective Till Date cannot be before Effective From Date"));
        }

        if (CollectionUtils.isEmpty(request.getRoleIds())) {

            throw new BusinessException(
                    new ErrorDetails(
                            AppConstant.ERROR_CODE_INVALID,
                            AppConstant.ERROR_TYPE_CODE_VALIDATION,
                            AppConstant.ERROR_TYPE_VALIDATION,
                            "At least one role must be selected"));
        }
    }

    private void saveRoleMappings(
            NotificationMaster notification,
            List<Long> roleIds,
            UserMaster user) {

        List<NotificationRoleMapping> mappings =
                new ArrayList<>();

        for (Long roleId : roleIds) {

            RoleMaster role =
                    roleMasterRepository.findById(Math.toIntExact(roleId))
                            .orElseThrow(() ->
                                    new BusinessException(
                                            new ErrorDetails(
                                                    AppConstant.ERROR_CODE_RESOURCE,
                                                    AppConstant.ERROR_TYPE_CODE_VALIDATION,
                                                    AppConstant.ERROR_TYPE_VALIDATION,
                                                    "Role not found")));

            NotificationRoleMapping mapping =
                    new NotificationRoleMapping();

            mapping.setNotification(notification);
            mapping.setRole(role);

            mapping.setCreatedBy(user.getUsername());
            mapping.setCreatedDate(LocalDateTime.now());

            mappings.add(mapping);
        }

        notificationRoleMappingRepository.saveAll(mappings);
    }

    private void createAuditLog(
            Long notificationId,
            String action,
            String actionBy,
            String remarks) {

        NotificationAuditLog audit =
                new NotificationAuditLog();

        audit.setNotificationId(notificationId);
        audit.setAction(action);
        audit.setActionBy(actionBy);
        audit.setRemarks(remarks);
        audit.setCreatedDate(LocalDateTime.now());

        notificationAuditLogRepository.save(audit);
    }
/*
    @Transactional
    public void saveViewLog(
            NotificationMaster notification,
            Long userId) {

        boolean exists =
                notificationViewLogRepository
                        .existsByNotificationIdAndUserUserId(
                                notification.getId(),
                                userId.intValue());

        if (exists) {
            return;
        }

        UserMaster user = getUser(userId);

        NotificationViewLog log =
                new NotificationViewLog();

        log.setNotification(notification);
        log.setUser(user);

        log.setViewedAt(LocalDateTime.now());

        log.setCreatedBy(user.getUsername());
        log.setCreatedDate(LocalDateTime.now());

        notificationViewLogRepository.save(log);
    }*/
private NotificationListResponse mapToListResponse(
        NotificationMaster notification) {

    NotificationListResponse response =
            new NotificationListResponse();

    response.setId(notification.getId());

    response.setNotificationNumber(
            notification.getNotificationNumber());

    response.setTitle(
            notification.getTitle());

    response.setStatus(
            notification.getStatus());

    response.setPopupNotification(
            notification.getPopupNotification());

    response.setEffectiveFrom(
            notification.getEffectiveFrom());

    response.setEffectiveTill(
            notification.getEffectiveTill());

    response.setIssuingAuthority(
            notification.getIssuingAuthority());

    response.setCreatedDate(
            notification.getCreatedDate());

    return response;
}


    private RoleDto mapToRoleDto(
            NotificationRoleMapping roleMapping) {

        RoleDto dto = new RoleDto();

        dto.setRoleId(
                Long.valueOf(roleMapping.getRole().getRoleId()));

        dto.setRoleName(
                roleMapping.getRole().getRoleName());

        return dto;
    }


    private AttachmentDto mapToAttachmentDto(
            NotificationAttachment attachment) {

        AttachmentDto dto =
                new AttachmentDto();

        dto.setId(
                attachment.getId());

        dto.setFileName(
                attachment.getFileName());

        dto.setBlobUrl(
                attachment.getBlobUrl());

        dto.setFileSize(
                attachment.getFileSize());

        return dto;
    }

    private NotificationDetailResponse
    mapToDetailResponse(
            NotificationMaster notification) {

        NotificationDetailResponse response =
                new NotificationDetailResponse();

        response.setId(notification.getId());

        response.setNotificationNumber(
                notification.getNotificationNumber());

        response.setTitle(
                notification.getTitle());

        response.setContent(
                notification.getContent());

        response.setStatus(
                notification.getStatus());

        response.setPopupNotification(
                notification.getPopupNotification());

        response.setIssuingAuthority(
                notification.getIssuingAuthority());

        response.setEffectiveFrom(
                notification.getEffectiveFrom());

        response.setEffectiveTill(
                notification.getEffectiveTill());

        response.setCreatedDate(
                notification.getCreatedDate());

        response.setRoles(
                notification.getRoleMappings()
                        .stream()
                        .map(this::mapToRoleDto)
                        .toList());

        response.setAttachments(
                notification.getAttachments()
                        .stream()
                        .map(this::mapToAttachmentDto)
                        .toList());

        return response;
    }


    @Override
    @Transactional
    public NotificationDetailResponse getNotificationDetails(
            Long notificationId,
            Long userId) {

        NotificationMaster notification =
                getNotification(notificationId);

        saveViewLog(notification, userId);

        return mapToDetailResponse(notification);
    }

    private void saveViewLog(
            NotificationMaster notification,
            Long userId) {

        boolean alreadyViewed =
                notificationViewLogRepository
                        .existsByNotificationIdAndUserUserId(
                                notification.getId(),
                                userId.intValue());

        if (alreadyViewed) {
            return;
        }

        UserMaster user =
                getUser(userId);

        NotificationViewLog viewLog =
                new NotificationViewLog();

        viewLog.setNotification(notification);

        viewLog.setUser(user);

        viewLog.setViewedAt(LocalDateTime.now());

        viewLog.setCreatedBy(user.getUsername());

        viewLog.setCreatedDate(LocalDateTime.now());

        notificationViewLogRepository.save(viewLog);

        createAuditLog(
                notification.getId(),
                "VIEW",
                user.getUsername(),
                "Notification Viewed");
    }

   /* private NotificationDetailResponse mapToDetailResponse(
            NotificationMaster notification) {

        NotificationDetailResponse response =
                new NotificationDetailResponse();

        response.setId(notification.getId());

        response.setNotificationNumber(
                notification.getNotificationNumber());

        response.setTitle(
                notification.getTitle());

        response.setContent(
                notification.getContent());

        response.setStatus(
                notification.getStatus());

        response.setPopupNotification(
                notification.getPopupNotification());

        response.setIssuingAuthority(
                notification.getIssuingAuthority());

        response.setEffectiveFrom(
                notification.getEffectiveFrom());

        response.setEffectiveTill(
                notification.getEffectiveTill());

        response.setCreatedDate(
                notification.getCreatedDate());

        response.setRoles(
                notification.getRoleMappings()
                        .stream()
                        .map(this::mapToRoleDto)
                        .toList());

        response.setAttachments(
                notification.getAttachments()
                        .stream()
                        .map(this::mapToAttachmentDto)
                        .toList());

        return response;
    }
*/


}
