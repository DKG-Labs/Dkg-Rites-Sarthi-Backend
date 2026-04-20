package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.WaterQualityTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WaterQualityTestRepository extends JpaRepository<WaterQualityTest, Long> {
    List<WaterQualityTest> findByCreatedByOrderByTestDateDesc(Integer createdBy);
}
