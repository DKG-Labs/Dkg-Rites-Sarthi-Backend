package com.sarthi.Sleeper.repository.FInalCallRepo;

import com.sarthi.Sleeper.entity.FInalCall.SleeperBatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SleeperBatchResultRepository extends JpaRepository<SleeperBatchResult, Long> {

    List<SleeperBatchResult> findBySleeperFinalResultId(Long sleeperFinalResultId);

    void deleteBySleeperFinalResultId(Long sleeperFinalResultId);
}
