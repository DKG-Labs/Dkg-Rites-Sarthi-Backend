package com.sarthi.repository;

import com.sarthi.entity.rawmaterial.InspectionModificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspectionModificationHistoryRepository extends JpaRepository<InspectionModificationHistory, Long> {
}
