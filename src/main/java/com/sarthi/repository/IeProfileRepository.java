package com.sarthi.repository;

import com.sarthi.entity.IeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IeProfileRepository extends JpaRepository<IeProfile,Long> {
    Optional<IeProfile> findFirstByEmployeeCode(String employeeCode);
    void deleteByEmployeeCode(String employeeCode);
}
