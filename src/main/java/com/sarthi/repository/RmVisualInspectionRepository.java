package com.sarthi.repository;

import com.sarthi.entity.RmVisualInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RmVisualInspection entity.
 */
@Repository
public interface RmVisualInspectionRepository extends JpaRepository<RmVisualInspection, Long> {

    List<RmVisualInspection> findByInspectionCallNo(String inspectionCallNo);

    List<RmVisualInspection> findByInspectionCallNoAndHeatNo(String inspectionCallNo, String heatNo);

    void deleteByInspectionCallNo(String inspectionCallNo);

    List<RmVisualInspection> findByInspectionCallNoIn(List<String> callNos);

    @Query("""
SELECT
    r.inspectionCallNo,
    SUM(r.weightRejected)
FROM RmVisualInspection r
WHERE r.inspectionCallNo IN :callNos
AND r.weightRejected IS NOT NULL
GROUP BY r.inspectionCallNo
""")
    List<Object[]> getVisualRejectedWeight(
            @Param("callNos") List<String> callNos);
}

