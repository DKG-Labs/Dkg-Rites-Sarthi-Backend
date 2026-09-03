package com.sarthi.Sleeper.repository.FInalCallRepo;

import com.sarthi.Sleeper.entity.FInalCall.SleeperFinalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface SleeperFinalResultRepository extends JpaRepository<SleeperFinalResult, Long> {

    Optional<SleeperFinalResult> findByCallNumber(String callNumber);

    @Query("""
        SELECT COALESCE(SUM(s.totalOfferedQuantity), 0)
        FROM SleeperFinalResult s
        WHERE s.poNo = :poNo 
          AND s.srNo = :srNo
          AND (:callNumber IS NULL OR s.callNumber <> :callNumber)
    """)
    BigDecimal getCumulativeOfferedQty(
        @Param("poNo") String poNo,
        @Param("srNo") String srNo,
        @Param("callNumber") String callNumber
    );

    @Query("""
        SELECT COALESCE(SUM(s.totalAccepted), 0)
        FROM SleeperFinalResult s
        WHERE s.poNo = :poNo 
          AND s.srNo = :srNo
          AND (:callNumber IS NULL OR s.callNumber <> :callNumber)
    """)
    BigDecimal getCumulativePassedQty(
        @Param("poNo") String poNo,
        @Param("srNo") String srNo,
        @Param("callNumber") String callNumber
    );

    @Query("""
        SELECT COALESCE(SUM(s.totalRejected), 0)
        FROM SleeperFinalResult s
        WHERE s.poNo = :poNo 
          AND s.srNo = :srNo
          AND (:callNumber IS NULL OR s.callNumber <> :callNumber)
    """)
    BigDecimal getCumulativeRejectedQty(
        @Param("poNo") String poNo,
        @Param("srNo") String srNo,
        @Param("callNumber") String callNumber
    );
}
