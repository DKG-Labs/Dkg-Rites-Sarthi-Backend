package com.sarthi.repository;

import com.sarthi.entity.UserProfileAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileAuditRepository extends JpaRepository<UserProfileAuditLog, Long> {
    List<UserProfileAuditLog> findByUserIdOrderByTimestampDesc(Integer userId);
}
