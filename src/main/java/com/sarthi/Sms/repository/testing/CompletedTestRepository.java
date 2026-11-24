package com.sarthi.Sms.repository.testing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

import com.sarthi.Sms.entity.testing.CompletedTest;
@Repository
public interface CompletedTestRepository extends JpaRepository<CompletedTest, Integer> {
    
    Optional<CompletedTest> findByHeatNumberAndStrandNumberAndSampleIdAndTestType(
        String heatNumber,
        Integer strandNumber,
        String sampleId,
        String testType
    );

    Optional<CompletedTest> findByHeatNumberAndStrandNumberAndSampleId(
        String heatNumber,
        Integer strandNumber,
        String sampleId
    );

    List<CompletedTest> findByHeatNumberAndStrandNumber(String heatNumber, Integer strandNumber);
    
    List<CompletedTest> findByDutyId(String dutyId);
    
    List<CompletedTest> findBySampleId(String sampleId);
    
    boolean existsByHeatNumberAndStrandNumberAndTestType(
        String heatNumber, 
        Integer strandNumber, 
        String testType
    );
}