package com.sarthi.Sleeper.repository;


import com.sarthi.Sleeper.entity.SampleCube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SampleCubeRepository extends JpaRepository<SampleCube, Long> {
    @Query("""
    SELECT c FROM SampleCube c
    WHERE c.sample.id IN :ids
""")
    List<SampleCube> findBySampleIds(List<Long> ids);
}
