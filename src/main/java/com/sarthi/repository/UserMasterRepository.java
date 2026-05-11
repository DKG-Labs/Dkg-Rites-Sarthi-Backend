package com.sarthi.repository;

import com.sarthi.entity.UserMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMasterRepository extends JpaRepository<UserMaster, Integer> {

    Optional<UserMaster> findFirstByUserName(String username);

    Optional<UserMaster> findByUserName(String username);

    Optional<UserMaster> findByUserId(Integer userId);

    java.util.List<UserMaster> findByUserIdIn(java.util.List<Integer> userIds);

    boolean existsByUserName(String vendorCode);

    Optional<UserMaster> findFirstByEmployeeCode(String employeeCode);
    
    UserMaster findByEmployeeCode(String employeeCode); // Keep for compatibility if needed elsewhere, but use findFirstBy in service

    java.util.List<UserMaster> findByRoleNameContaining(String roleName);

    @Query("""
        SELECT r.roleName
        FROM UserRoleMaster ur
        JOIN RoleMaster r ON ur.roleId = r.roleId
        WHERE ur.userId = :userId
    """)
    String findRoleNameByUserId(Integer userId);


    @Query("SELECT u.employeeCode FROM UserMaster u WHERE u.userId = :userId")
    String findEmployeeCodeByUserId(Integer userId);
}
