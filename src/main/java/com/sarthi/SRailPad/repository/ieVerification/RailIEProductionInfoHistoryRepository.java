package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailIEProductionInfoHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RailIEProductionInfoHistoryRepository extends JpaRepository<RailIEProductionInfoHistory, Long> {
}
