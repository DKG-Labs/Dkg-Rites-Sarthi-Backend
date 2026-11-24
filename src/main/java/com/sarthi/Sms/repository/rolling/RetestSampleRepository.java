package com.sarthi.Sms.repository.rolling;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.rolling.RetestSample;
@Repository
public interface RetestSampleRepository extends JpaRepository<RetestSample, Integer> {

    // Find all retest samples for a given heat number
    List<RetestSample> findByHeatNumber(String heatNumber);

    // Find all retest samples for a given heat number, test type, and sample lot
    List<RetestSample> findByHeatNumberAndTestTypeAndSampleLot(
        String heatNumber,
        String testType,
        String sampleLot
    );

    // Find the first retest sample by ID to get the group information
    @Query("SELECT rs FROM RetestSample rs WHERE rs.retestSampleId = :retestSampleId")
    RetestSample findByRetestSampleId(@Param("retestSampleId") Integer retestSampleId);

    // Find existing retest sample for duplicate check
    List<RetestSample> findByHeatNumberAndStrandNumberAndTestTypeAndSampleLot(
        String heatNumber,
        String strandNumber,
        String testType,
        String sampleLot
    );
}