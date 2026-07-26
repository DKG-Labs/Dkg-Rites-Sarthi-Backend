package com.sarthi.repository;

import com.sarthi.entity.Mail.MailNotificationMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailNotificationMasterRepository extends JpaRepository<MailNotificationMaster, Long> {
    List<MailNotificationMaster> findByStatus(String failed);
}
