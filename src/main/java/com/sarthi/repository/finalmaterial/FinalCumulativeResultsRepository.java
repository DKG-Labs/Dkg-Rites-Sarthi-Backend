package com.sarthi.repository.finalmaterial;

import com.sarthi.entity.finalmaterial.FinalCumulativeResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository for Final Cumulative Results
 */
@Repository
public interface FinalCumulativeResultsRepository extends JpaRepository<FinalCumulativeResults, Long> {

    /**
     * Find cumulative results by inspection call number
     */
    Optional<FinalCumulativeResults> findByInspectionCallNo(String inspectionCallNo);

    /**
     * Find cumulative results by PO number
     */
    List<FinalCumulativeResults> findByPoNo(String poNo);

    /**
     * Check if cumulative results exist for a call
     */
    boolean existsByInspectionCallNo(String inspectionCallNo);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(fcr.qtyNowPassed) FROM FinalCumulativeResults fcr")
    Long sumTotalQtyNowPassed();

    @org.springframework.data.jpa.repository.Query("SELECT SUM(fcr.qtyNowPassed) FROM FinalCumulativeResults fcr WHERE fcr.createdAt >= :date")
    Long sumTotalQtyNowPassedLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(fcr.qtyNowRejected), SUM(fcr.qtyNowOffered) FROM FinalCumulativeResults fcr WHERE fcr.createdAt >= :date")
    List<Object[]> sumFinalRejectionLast30Days(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDateTime date);
}
