package com.sarthi.Sleeper.repository.ProductionDeclaration;

import com.sarthi.Sleeper.dto.FinalInspectionDtos.BatchTestingListResponseDto;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionBenchGroup;
import com.sarthi.Sleeper.entity.ProductionDeclaration.ProductionDeclaration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductionBenchGroupRepository extends JpaRepository<ProductionBenchGroup, Long> {

    ProductionBenchGroup findByBenchNo(String benchNo);

    @Query("SELECT DISTINCT b.sleeperType FROM ProductionBenchGroup b " +
            "WHERE b.chamber.declaration.batchNumber = :batchNo")
    List<String> findSleeperTypesByBatch(@Param("batchNo") String batchNo);

    @Query("SELECT DISTINCT b.sleeperType FROM ProductionBenchGroup b " +
            "WHERE b.chamber.declaration.batchNumber = :batchNo " +
            "AND b.benchNo = :benchNo")
    List<String> findSleeperTypes(@Param("batchNo") String batchNo,
                                  @Param("benchNo") String benchNo);
}
