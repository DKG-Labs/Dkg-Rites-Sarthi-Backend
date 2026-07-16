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
}
