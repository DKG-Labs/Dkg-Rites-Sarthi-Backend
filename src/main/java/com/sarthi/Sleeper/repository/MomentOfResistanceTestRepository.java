package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MomentOfResistance;
import com.sarthi.Sleeper.entity.MomentOfResistanceTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MomentOfResistanceTestRepository extends JpaRepository<MomentOfResistanceTest, Long> {
    @Query("""
    SELECT m FROM MomentOfResistanceTest m
    WHERE m.plantId = :plantId
    AND m.vendorCode = :vendorCode
    AND m.shift = :shift
    AND m.createdBy = :createdBy
    AND m.createdDate BETWEEN :startOfDay AND :endOfDay
""")
    List<MomentOfResistanceTest> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
