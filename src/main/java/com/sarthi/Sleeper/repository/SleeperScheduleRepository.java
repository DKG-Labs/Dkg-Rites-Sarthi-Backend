package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.FInalCall.SleeperSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SleeperScheduleRepository extends JpaRepository<SleeperSchedule, Long> {
    boolean existsByCallNo(String callNo);
}
