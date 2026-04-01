package com.sarthi.repository;

import com.sarthi.entity.UserMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMasterRepository extends JpaRepository<UserMaster, Integer> {
    Optional<UserMaster> findByUserName(String userName);

    Optional<UserMaster> findByUserId(Integer userId);

    java.util.List<UserMaster> findByUserIdIn(java.util.List<Integer> userIds);

    boolean existsByUserName(String vendorCode);

    UserMaster findByEmployeeCode(String employeeCode);

    java.util.List<UserMaster> findByRoleNameContaining(String roleName);
}
