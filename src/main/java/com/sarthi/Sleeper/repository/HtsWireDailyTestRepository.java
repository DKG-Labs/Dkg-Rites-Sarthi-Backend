package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.HtsWireDailyTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HtsWireDailyTestRepository extends JpaRepository<HtsWireDailyTest, Long> {
    List<HtsWireDailyTest> findByConsignmentNo(String consignmentNo);
    Optional<HtsWireDailyTest> findByRequestId(Long requestId);
}
