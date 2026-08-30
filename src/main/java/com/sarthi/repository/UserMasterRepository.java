package com.sarthi.repository;

import com.sarthi.entity.UserMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMasterRepository extends JpaRepository<UserMaster, Integer> {

    Optional<UserMaster> findFirstByUserName(String username);

    Optional<UserMaster> findByUserId(Integer userId);

    java.util.List<UserMaster> findByUserIdIn(java.util.List<Integer> userIds);

    boolean existsByUserName(String vendorCode);

    Optional<UserMaster> findFirstByEmployeeCode(String employeeCode);

    Optional<UserMaster> findFirstByEmail(String email);

    UserMaster findByEmployeeCode(String employeeCode); // Keep for compatibility if needed elsewhere, but use findFirstBy in service

    java.util.List<UserMaster> findByRoleNameContaining(String roleName);

    @Query(value = """
        SELECT DISTINCT um.* 
        FROM user_master um 
        JOIN user_role_master urm ON um.userid = urm.userid 
        JOIN role_master rm ON urm.roleid = rm.roleid 
        WHERE rm.rolename = :roleName 
           OR LOWER(REPLACE(rm.rolename, ' ', '')) = LOWER(REPLACE(:roleName, ' ', ''))
           OR LOWER(rm.rolename) LIKE LOWER(CONCAT('%', :roleName, '%'))
    """, nativeQuery = true)
    java.util.List<UserMaster> findUsersByRoleNameViaJoin(@org.springframework.data.repository.query.Param("roleName") String roleName);

    @Query("""
                SELECT r.roleName
                FROM UserRoleMaster ur
                JOIN RoleMaster r ON ur.roleId = r.roleId
                WHERE ur.userId = :userId
            """)
    String findRoleNameByUserId(Integer userId);


    @Query("SELECT u.employeeCode FROM UserMaster u WHERE u.userId = :userId")
    String findEmployeeCodeByUserId(Integer userId);

    @Query("""
            select rm.roleName
            from UserRoleMaster urm
            join RoleMaster rm
            on urm.roleId = rm.roleId
            where urm.userId = :userId
            """)
    List<String> findRoleNamesByUserId(Integer userId);

//    @Query("""
//            SELECT COUNT(u) > 0
//            FROM UserMaster u
//            WHERE u.userid = :userId
//            """)
//    boolean existsUser(Integer userId);

    @Query("""
SELECT u.employeeCode
FROM UserMaster u
WHERE u.userId = :userId
""")
    String findEmployeeCode(Integer userId);

    @Query("""
SELECT u
FROM UserMaster u
JOIN UserRoleMaster ur
ON u.userId = ur.userId
WHERE ur.roleId = :roleId
ORDER BY u.employeeCode
""")
    List<UserMaster> findUsersByRoleId(Integer roleId);

    @Query("""
SELECT u.userId, u.employeeCode, u.fullName
FROM UserMaster u
JOIN UserRoleMaster ur
ON u.userId = ur.userId
WHERE ur.roleId = :roleId
ORDER BY u.employeeCode
""")
    List<Object[]> findUserDetailsByRoleId(@org.springframework.data.repository.query.Param("roleId") Integer roleId);

    Optional<UserMaster> findByUserName(String vendorCode);
}
