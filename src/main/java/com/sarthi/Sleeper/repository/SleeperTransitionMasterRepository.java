package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.SleeperTransitionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperTransitionMasterRepository extends JpaRepository<SleeperTransitionMaster, Long> {
    Optional<SleeperTransitionMaster> findFirstByWorkflowIdAndCurrentActionOrderByTransitionOrderAsc(int i, String createdType);

   List<SleeperTransitionMaster> findByWorkflowIdAndCurrentRoleIdAndCurrentAction(int i, Integer roleId, String action);

    Optional<SleeperTransitionMaster> findByWorkflowIdAndCurrentRoleIdAndCurrentActionAndNextAction(int i, Integer roleId, String action, String action1);
}
