package com.sarthi.SRailPad.repository.inspectionCall;

import com.sarthi.SRailPad.entity.inspectionCall.RailProcessCallHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RailProcessCallHistoryRepository extends JpaRepository<RailProcessCallHistory, Long> {
    List<RailProcessCallHistory> findByProcessCallDetail_InspectionCall_CallNoOrderByUpdatedAtDesc(String callNo);
}
