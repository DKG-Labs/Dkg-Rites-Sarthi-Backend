package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.InspectionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InspectionModuleRepository extends JpaRepository<InspectionModule, Long> {
}
