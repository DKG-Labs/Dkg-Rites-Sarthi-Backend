package com.sarthi.repository;

import com.sarthi.entity.InspectionCallDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for InspectionCallDetails (Section B) entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface InspectionCallDetailsRepository extends JpaRepository<InspectionCallDetails, Long> {

    /**
     * Find InspectionCallDetails by inspection call number
     */
    Optional<InspectionCallDetails> findByInspectionCallNo(String inspectionCallNo);

    /**
     * Find the most recent InspectionCallDetails by inspection call number (to avoid NonUniqueResultException)
     */
    Optional<InspectionCallDetails> findFirstByInspectionCallNoOrderByIdDesc(String inspectionCallNo);

    /**
     * Check if record exists by inspection call number
     */
    boolean existsByInspectionCallNo(String inspectionCallNo);

    /**
     * Find all InspectionCallDetails by status
     */
    List<InspectionCallDetails> findByStatus(String status);

    /**
     * Find all InspectionCallDetails by product type
     */
    List<InspectionCallDetails> findByProductType(String productType);

    /**
     * Find InspectionCallDetails with SubPoDetails eagerly loaded
     */
    @Query("SELECT icd FROM InspectionCallDetails icd LEFT JOIN FETCH icd.subPoDetails WHERE icd.inspectionCallNo = :callNo")
    Optional<InspectionCallDetails> findByInspectionCallNoWithSubPoDetails(@Param("callNo") String callNo);

    /**
     * Find all InspectionCallDetails by MainPoInformation ID
     */
    Optional<InspectionCallDetails> findByMainPoInformationId(Long mainPoId);
}

