package com.sarthi.Sleeper.repository.ProductionDeclaration;

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
            null
            )
            FROM ProductionDeclaration d
            JOIN d.chambers c
            JOIN c.benchGroups b
            JOIN b.sleepers s
            JOIN SleeperWorkflowTransaction w
                 ON w.requestId = CAST(d.id as string)
            WHERE w.status = 'Completed'
            GROUP BY d.id,d.batchNumber,b.sleeperType,d.totalCastedSleepers
            """)
    List<BatchTestingListResponseDto> getAllBatchTesting();

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

    @Query("SELECT DISTINCT b.benchNo FROM ProductionBenchGroup b " +
            "WHERE b.chamber.declaration.batchNumber = :batchNo")
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


}
