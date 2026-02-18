package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.SampleOtherBench;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleOtherBenchRepository extends JpaRepository<SampleOtherBench, Long> {
}
