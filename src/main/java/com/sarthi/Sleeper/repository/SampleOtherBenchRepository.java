package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.SampleOtherBench;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleOtherBenchRepository extends JpaRepository<SampleOtherBench, Long> {
    @Query("""
    SELECT b FROM SampleOtherBench b
    WHERE b.sample.id IN :ids
""")
    List<SampleOtherBench> findBySampleIds(List<Long> ids);
}
