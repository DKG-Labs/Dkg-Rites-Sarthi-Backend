package com.sarthi.repository;

import com.sarthi.entity.RmMaterialTesting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RmMaterialTesting entity.
 */
@Repository
public interface RmMaterialTestingRepository extends JpaRepository<RmMaterialTesting, Long> {

    List<RmMaterialTesting> findByInspectionCallNo(String inspectionCallNo);

    List<RmMaterialTesting> findByInspectionCallNoAndHeatNo(String inspectionCallNo, String heatNo);

    void deleteByInspectionCallNo(String inspectionCallNo);

    List<RmMaterialTesting> findByInspectionCallNoIn(List<String> callNos);

    @Query("""
SELECT DISTINCT r.inspectionCallNo
FROM RmMaterialTesting r
WHERE r.inspectionCallNo IN :callNos
AND (
       r.inclusionA > 2
    OR r.inclusionB > 2
    OR r.inclusionC > 2
    OR r.inclusionD > 2
)
""")
    List<String> getInclusionDefectCalls(
            @Param("callNos") List<String> callNos);

    @Query("""
SELECT DISTINCT r.inspectionCallNo
FROM RmMaterialTesting r
WHERE r.inspectionCallNo IN :callNos
AND r.grainSize < 6
""")
    List<String> getGrainSizeDefectCalls(
            @Param("callNos") List<String> callNos);

    @Query("""
SELECT DISTINCT r.inspectionCallNo
FROM RmMaterialTesting r
WHERE r.inspectionCallNo IN :callNos
AND r.decarb > 2.0
""")
    List<String> getDecarbDefectCalls(
            @Param("callNos") List<String> callNos);
}

