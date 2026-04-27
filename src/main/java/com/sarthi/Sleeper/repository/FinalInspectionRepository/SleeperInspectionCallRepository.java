package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SleeperInspectionCallRepository extends JpaRepository<SleeperInspectionCall, Long> {
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<SleeperInspectionCall> findByCreatedBy(Long createdBy);

    Optional<SleeperInspectionCall> findByCallNo(String callNo);

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.goodSleepers s
""")
    List<Long> findAllGoodSleeperIds();

    @Query("""
SELECT DISTINCT s.sleeperId
FROM SleeperInspectionCallBatch b
JOIN b.badSleepers s
""")
    List<Long> findAllBadSleeperIds();
}
