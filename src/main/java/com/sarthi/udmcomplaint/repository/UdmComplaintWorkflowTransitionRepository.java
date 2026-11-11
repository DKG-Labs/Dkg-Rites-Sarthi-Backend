package com.sarthi.udmcomplaint.repository;

import com.sarthi.udmcomplaint.entity.UdmComplaintWorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UdmComplaintWorkflowTransitionRepository extends JpaRepository<UdmComplaintWorkflowTransition, Long> {

    List<UdmComplaintWorkflowTransition> findByUdmRejectionIdOrderByCreatedDateAsc(Long udmRejectionId);
}
