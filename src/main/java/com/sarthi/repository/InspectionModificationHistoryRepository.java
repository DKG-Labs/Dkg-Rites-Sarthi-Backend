package com.sarthi.repository;

import com.sarthi.entity.rawmaterial.InspectionModificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspectionModificationHistoryRepository extends JpaRepository<InspectionModificationHistory, Long> {

    /** Highest edit version recorded for a call (0 if never edited). */
    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(MAX(h.modificationVersion), 0) FROM InspectionModificationHistory h WHERE h.inspectionCallId = :callId")
    Integer findMaxVersion(@org.springframework.data.repository.query.Param("callId") Long callId);
}
