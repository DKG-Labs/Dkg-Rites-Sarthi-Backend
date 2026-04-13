package com.sarthi.Sleeper.repository.ProductionDeclaration;

import com.sarthi.Sleeper.dto.BatchWithIdProjection;
import com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionDeclarationRepository extends JpaRepository<ProductionDeclaration, Long> {

    /*
     * @Query("""
     * SELECT new
     * com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto(
     * d.id,
     * d.batchNumber,
     * b.sleeperType,
     * d.totalCastedSleepers,
     * COUNT(s.id),
     * 0.0,
     * 'Pending',
     * null
     * )
     * FROM ProductionDeclaration d
     * JOIN d.chambers c
     * JOIN c.benchGroups b
     * JOIN b.sleepers s
     * GROUP BY d.id,d.batchNumber,b.sleeperType,d.totalCastedSleepers
     * """)
     * List<BatchTestingListResponseDto> getAllBatchTesting();
     */
    @Query("""
            SELECT new com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto(
            d.id,
            d.batchNumber,
            b.sleeperType,
            d.totalCastedSleepers,
            COUNT(s.id),
            0.0,
            'Pending',
            null,
            d.plantId
            )
            FROM ProductionDeclaration d
            JOIN d.chambers c
            JOIN c.benchGroups b
            JOIN b.sleepers s
            JOIN SleeperWorkflowTransaction w
                 ON w.requestId = CAST(d.id as string)
            WHERE w.status = 'Completed'
            GROUP BY d.id,d.batchNumber,b.sleeperType,d.totalCastedSleepers, d.plantId
            """)
    List<BatchTestingListResponseDto> getAllBatchTesting();

    @Query("""
SELECT new com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto(
d.id,
d.batchNumber,
g.sleeperType,
d.totalCastedSleepers,
COUNT(s.id),
0.0,
'Pending',
null,
d.plantId
)
FROM ProductionDeclaration d
JOIN d.gangs g
JOIN g.sleepers s
JOIN SleeperWorkflowTransaction w
     ON w.requestId = CAST(d.id as string)
WHERE w.status = 'Completed'
GROUP BY d.id,d.batchNumber,g.sleeperType,d.totalCastedSleepers, d.plantId
""")
    List<BatchTestingListResponseDto> getLongLineBatchTesting();
    @Query("""
            SELECT d
            FROM ProductionDeclaration d
            WHERE d.id = :batchId
            """)
    ProductionDeclaration findBatchById(Long batchId);

    List<ProductionDeclaration> findByCreatedBy(Long createdBy);

    List<ProductionDeclaration> findByIdIn(List<Long> ids);

    @Query("SELECT DISTINCT p.batchNumber FROM ProductionDeclaration p " +
            "WHERE p.createdBy = :createdBy AND p.castingDate = :castingDate")
    List<String> findBatchNumbers(@Param("createdBy") Long createdBy,
                                  @Param("castingDate") LocalDate castingDate);

//    @Query("SELECT DISTINCT b.benchNo FROM ProductionBenchGroup b " +
//            "WHERE b.chamber.declaration.batchNumber = :batchNo")
//    List<String> findBenchNumbers(String batchNo);

    @Query(value = """
SELECT DISTINCT b.bench_no
FROM production_bench_group b
JOIN production_stress_chamber c 
    ON b.chamber_id = c.id
JOIN production_declaration d 
    ON c.declaration_id = d.id
WHERE d.batch_number = :batchNo
""", nativeQuery = true)
    List<String> findBenchNumbers(String batchNo);

    @Query(value = """
SELECT DISTINCT p.batch_number
FROM production_declaration p
JOIN sleeper_workflow_transaction w 
  ON w.request_id = p.id
WHERE p.created_by = :vendorId
  AND p.casting_date = :castingDate
  AND p.plant_id = :plantId
  AND p.production_unit = :productionUnit
  AND w.module_id = 11
  AND LOWER(w.status) = 'completed'
  AND w.workflow_transition_id = (
      SELECT MAX(w2.workflow_transition_id)
      FROM sleeper_workflow_transaction w2
      WHERE w2.request_id = p.id
        AND w2.module_id = 11
  )
""", nativeQuery = true)
    List<String> findValidBatchNumbers(
            Long vendorId,
            LocalDate castingDate,
            String plantId,
            String productionUnit
    );


    ProductionDeclaration findByBatchNumber(String batchNo);


   @Query(value = """
SELECT DISTINCT b.bench_no AS value, NULL AS gang_from, NULL AS gang_to
FROM production_bench_group b
JOIN production_stress_chamber c ON b.chamber_id = c.id
JOIN production_declaration d ON c.declaration_id = d.id
WHERE d.batch_number = :batchNo

UNION

SELECT DISTINCT NULL AS value, g.gang_from, g.gang_to
FROM production_longline_gang g
JOIN production_declaration d ON g.declaration_id = d.id
WHERE d.batch_number = :batchNo
""", nativeQuery = true)
    List<Object[]> findBenchAndGangRaw(String batchNo);

    @Query("""
SELECT DISTINCT g.sleeperType 
FROM ProductionLongLineGang g
WHERE g.declaration.batchNumber = :batchNo
AND :benchNo BETWEEN g.gangFrom AND g.gangTo
""")
    List<String> findSleeperTypes(String batchNo, Integer benchNo);
 /*  @Query("""
SELECT DISTINCT g.sleeperType 
FROM ProductionLongLineGang g
WHERE g.declaration.batchNumber = :batchNo
AND (
        (g.mode = 'RANGE' AND g.gangFrom IS NOT NULL AND g.gangTo IS NOT NULL 
             AND :benchNo BETWEEN g.gangFrom AND g.gangTo)
     OR (g.mode = 'SINGLE' AND g.gangNo IS NOT NULL AND g.gangNo = :benchNo)
)
""")
   List<String> findSleeperTypes(String batchNo, Integer benchNo);
*/
    @Query(value = """
SELECT DISTINCT g.gang_from, g.gang_to
FROM production_longline_gang g
JOIN production_declaration d 
    ON g.declaration_id = d.id
WHERE d.batch_number = :batchNo
""", nativeQuery = true)
    List<Object[]> findGangRanges(String batchNo);

    @Query(value = """
SELECT DISTINCT 
    p.batch_number AS batchNumber,
    p.id AS id
FROM production_declaration p
JOIN sleeper_workflow_transaction w 
  ON w.request_id = p.id
WHERE p.created_by = :vendorId
  AND p.casting_date = :castingDate
  AND p.plant_id = :plantId
  AND p.production_unit = :productionUnit
  AND w.module_id = 11
  AND LOWER(w.status) = 'completed'
  AND w.workflow_transition_id = (
      SELECT MAX(w2.workflow_transition_id)
      FROM sleeper_workflow_transaction w2
      WHERE w2.request_id = p.id
        AND w2.module_id = 11
  )
""", nativeQuery = true)
    List<BatchWithIdProjection> findBatchWithId(
            Long vendorId,
            LocalDate castingDate,
            String plantId,
            String productionUnit
    );

    ProductionDeclaration findByBatchNumberAndProductionUnit(String batchNo, String productionUnit);
}
