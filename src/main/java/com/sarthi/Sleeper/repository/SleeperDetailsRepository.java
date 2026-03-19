package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.LonglineMaster;
import com.sarthi.Sleeper.entity.SleeperDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface SleeperDetailsRepository extends JpaRepository<SleeperDetails, Long> {
    void deleteByLonglineMaster(LonglineMaster entity);

    List<SleeperDetails> findByLonglineMaster(LonglineMaster longlineMaster);
}
