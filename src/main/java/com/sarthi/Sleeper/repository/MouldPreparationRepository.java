package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.MouldPreparation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MouldPreparationRepository extends JpaRepository<MouldPreparation, Long> {

    @Query("""
    SELECT m FROM MouldPreparation m
    WHERE m.plantId = :plantId
    AND m.vendorCode = :vendorCode
    AND m.shift = :shift
    AND m.createdBy = :createdBy
    AND m.createdDate BETWEEN :startOfDay AND :endOfDay
    AND m.status = 'A'
""")
    List<MouldPreparation> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

}
