package com.sarthi.repository;

import com.sarthi.entity.UserRoleMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserRoleMasterRepository extends JpaRepository<UserRoleMaster, Integer> {
    List<UserRoleMaster> findByUserId(Integer userId);

    boolean existsByUserIdAndRoleId(Integer userId, Integer roleId);

    @Modifying
    @Transactional
    void deleteByUserId(Integer userId);


    List<Long> findRoleIdsByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT urm.userid, rm.rolename 
        FROM user_role_master urm 
        JOIN role_master rm ON urm.roleid = rm.roleid
    """, nativeQuery = true)
    List<Object[]> findAllUserRolesWithRoleNames();

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT urm.userid, rm.rolename 
        FROM user_role_master urm 
        JOIN role_master rm ON urm.roleid = rm.roleid
        WHERE urm.userid IN (:userIds)
    """, nativeQuery = true)
    List<Object[]> findUserRolesByUserIds(@org.springframework.data.repository.query.Param("userIds") List<Integer> userIds);
}
