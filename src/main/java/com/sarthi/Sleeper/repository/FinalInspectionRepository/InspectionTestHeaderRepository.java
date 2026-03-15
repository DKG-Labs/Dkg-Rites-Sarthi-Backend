package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.InspectionTestHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspectionTestHeaderRepository extends JpaRepository<InspectionTestHeader, Long> {
}
