package com.sarthi.repository;

import com.sarthi.entity.UserMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMasterRepository extends JpaRepository<UserMaster, Integer> {
    Optional<UserMaster> findByUserName(String userName);

    Optional<UserMaster> findByUserId(Integer userId);

    boolean existsByUserName(String vendorCode);

    UserMaster findByEmployeeCode(String employeeCode);

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
