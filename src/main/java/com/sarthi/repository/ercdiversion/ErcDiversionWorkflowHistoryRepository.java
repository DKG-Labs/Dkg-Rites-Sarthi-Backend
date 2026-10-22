package com.sarthi.repository.ercdiversion;

import com.sarthi.entity.ercdiversion.ErcDiversionWorkflowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErcDiversionWorkflowHistoryRepository extends JpaRepository<ErcDiversionWorkflowHistory, Long> {

    List<ErcDiversionWorkflowHistory> findByDiversionRequestIdOrderByCreatedDateAsc(Long diversionRequestId);
}
