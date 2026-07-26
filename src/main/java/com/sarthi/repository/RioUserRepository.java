package com.sarthi.repository;

import com.sarthi.entity.RioUser;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@ReadingConverter
public interface RioUserRepository extends JpaRepository<RioUser, Long> {
    Optional<RioUser> findFirstByEmployeeCode(String employeeCode);
    Optional<RioUser> findByEmployeeCode(String employeeCode);

    boolean existsByRioAndEmployeeCode(String rio, String employeeCode);

    List<RioUser> findByRio(String rio);
}
