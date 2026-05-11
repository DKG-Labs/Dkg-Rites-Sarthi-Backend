package com.sarthi.SRailPad.repository;

import com.sarthi.SRailPad.entity.RailTransitionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RailTransitionMasterRepository extends JpaRepository<RailTransitionMaster, Integer> {
    Optional<RailTransitionMaster> findFirstByWorkflowIdAndCurrentActionOrderByTransitionOrderAsc(int i, String create);

    Optional<RailTransitionMaster> findByWorkflowIdAndCurrentRoleIdAndCurrentActionAndNextAction(int i, Integer currentRoleId, String action, String action1);

    Optional<RailTransitionMaster>
    findByWorkflowIdAndCurrentRoleIdAndNextAction(
            Integer workflowId,
            Integer currentRoleId,
            String nextAction
    );

    List<RailTransitionMaster> findByWorkflowIdAndCurrentRoleIdAndCurrentAction(int i, Integer roleId, String action);
}
