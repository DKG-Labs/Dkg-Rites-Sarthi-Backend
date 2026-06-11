package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.InspectionReasonMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspectionReasonMasterRepository extends JpaRepository<InspectionReasonMaster, Long> {
}
