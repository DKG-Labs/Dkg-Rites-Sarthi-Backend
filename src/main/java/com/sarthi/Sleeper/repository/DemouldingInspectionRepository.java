package com.sarthi.Sleeper.repository;



import com.sarthi.Sleeper.entity.DemouldingInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DemouldingInspectionRepository extends JpaRepository<DemouldingInspection, Long> {

    @Query("""
    SELECT d FROM DemouldingInspection d
    WHERE d.plantId = :plantId
    AND d.vendorCode = :vendorCode
    AND d.shift = :shift
    AND d.createdBy = :createdBy
    AND d.createdDate BETWEEN :startOfDay AND :endOfDay
""")
    List<DemouldingInspection> findTodayData(
            String plantId,
            String vendorCode,
            String shift,
            String createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("""
    SELECT d FROM DemouldingInspection d
    WHERE d.plantId = :plantId
    AND d.vendorCode = :vendorCode
    AND d.shift = :shift
    AND d.createdBy = :createdBy
    AND d.createdDate BETWEEN :startOfDay AND :endOfDay
 
""")
    List<DemouldingInspection> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            String createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("""
SELECT COUNT(d) > 0 
FROM DemouldingInspection d 
WHERE d.batchNo = :batchNo
""")
    boolean existsDemoulding(String batchNo);

    @Query("""
SELECT COUNT(d.id)
FROM DemouldingDefectiveSleeper d
WHERE d.inspection.batchNo = :batchNo
""")
    Long countDemouldingRejected(String batchNo);


}
