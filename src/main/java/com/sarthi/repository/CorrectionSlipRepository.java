package com.sarthi.repository;

import com.sarthi.entity.CorrectionSlip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorrectionSlipRepository extends JpaRepository<CorrectionSlip, Long> {

    /**
     * Find all correction rows for a given call number, ordered by creation time.
     */
    List<CorrectionSlip> findByCallNoOrderByCreatedAtAsc(String callNo);

    /**
     * Delete all existing rows for a call number (used before re-saving all rows).
     */
    void deleteAllByCallNo(String callNo);
}
