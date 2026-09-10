package com.sarthi.Sleeper.repository.FInalCallRepo;

import com.sarthi.Sleeper.entity.FInalCall.IEBatchSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IEBatchSummaryRepository extends JpaRepository<IEBatchSummary, Long> {
    List<IEBatchSummary> findByCallNo(String callNo);
    Optional<IEBatchSummary> findByCallNoAndBatchNo(String callNo, String batchNo);
}
