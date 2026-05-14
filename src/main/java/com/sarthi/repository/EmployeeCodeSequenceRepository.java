package com.sarthi.repository;

import com.sarthi.entity.EmployeeCodeSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeCodeSequenceRepository extends JpaRepository<EmployeeCodeSequence, Long> {

    Optional<EmployeeCodeSequence>
    findByRoleCodeAndZoneCode(
            String roleCode,
            String zoneCode);
}
