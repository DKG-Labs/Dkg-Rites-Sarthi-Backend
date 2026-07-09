package com.sarthi.repository;

import com.sarthi.entity.TransitionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransitionMasterRepository extends JpaRepository<TransitionMaster, Integer> {
    Optional<TransitionMaster> findByWorkflowIdAndTransitionOrder(Integer workflowId, int i);

    TransitionMaster findByTransitionName(String fixRouting);

    List<TransitionMaster> findByWorkflowId(Integer workflowId);

    TransitionMaster findByTransitionNameAndWorkflowId(String cancelCall, Integer workflowId);

    List<TransitionMaster> findByWorkflowIdAndCurrentRoleIdAndCurrentAction(Integer workflowId, Integer nextRoleId, String action);
}
