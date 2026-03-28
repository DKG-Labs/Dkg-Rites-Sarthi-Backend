package com.sarthi.repository;

import com.sarthi.entity.UserRoleMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleMasterRepository extends JpaRepository<UserRoleMaster, Integer> {
    List<UserRoleMaster> findByUserId(Integer userId);

    boolean existsByUserIdAndRoleId(Integer userId, Integer roleId);
}
