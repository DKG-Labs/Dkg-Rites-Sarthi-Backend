package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.dto.BatchIdNumberDto;
import com.sarthi.Sleeper.entity.BatchWeighment.BatchWeighment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BatchWeighmentRepository extends JpaRepository<BatchWeighment, Long> {
    @Query("""
    SELECT b FROM BatchWeighment b
    WHERE b.plantId = :plantId
    AND b.vendorCode = :vendorCode
    AND b.shift = :shift
    AND b.createdBy = :createdBy
    AND b.createdDate BETWEEN :startOfDay AND :endOfDay
""")
    List<BatchWeighment> findByDate(
            String plantId,
            String vendorCode,
            String shift,
            int createdBy,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("SELECT new com.sarthi.Sleeper.dto.BatchIdNumberDto(b.id, b.batchNumber) FROM BatchWeighment b")
    List<BatchIdNumberDto> findAllBatchIdsAndNumbers();

    @Query("SELECT new com.sarthi.Sleeper.dto.BatchIdNumberDto(b.id, b.batchNumber) " +
            "FROM BatchWeighment b " +
            "WHERE b.entryDate = :entryDate AND b.location = :location")
    List<BatchIdNumberDto> findBatchIdsAndNumbersByDateAndLocation(
            @Param("entryDate") LocalDate entryDate,
            @Param("location") String location);
}
