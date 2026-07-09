package com.sarthi.repository;

import com.sarthi.entity.IeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IeProfileRepository extends JpaRepository<IeProfile,Long> {
    void deleteByEmployeeCode(String employeeCode);
}
