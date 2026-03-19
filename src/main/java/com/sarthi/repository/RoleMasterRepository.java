package com.sarthi.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.sarthi.entity.RoleMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleMasterRepository extends JpaRepository<RoleMaster, Integer> {


     Optional<RoleMaster> findByRoleName(String roleName);

    Optional<RoleMaster> findByRoleId(Integer roleId);
}
