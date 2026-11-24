package com.sarthi.Sms.repository.rolling;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.dto.rolling.TestSampleAcptEntity;
import com.sarthi.Sms.dto.rolling.TestSampleAcptId;
@Repository
public interface TestSampleAcptRepository extends JpaRepository<TestSampleAcptEntity, Integer>{

    // Find by individual fields now that entity structure is fixed
    List<TestSampleAcptEntity> findBySampleId(String sampleId);
    List<TestSampleAcptEntity> findByHeatNo(String heatNo);

    @Query("SELECT t FROM TestSampleAcptEntity t WHERE t.heatNo = :heatNo AND t.strand = :strand AND t.sampleType = :sampleType AND t.sampleLot = :sampleLot")
    List<TestSampleAcptEntity> findDtl(
        @Param("heatNo") String heatNo,
        @Param("strand") Integer strand,
        @Param("sampleType") String sampleType,
        @Param("sampleLot") String sampleLot
    );

    // Find by rolling_test_sample_id (primary key)
    Optional<TestSampleAcptEntity> findById(Integer rollingTestSampleId);

    // Find by composite key fields (replacement for findByTsai)
    @Query("SELECT t FROM TestSampleAcptEntity t WHERE t.sampleId = :sampleId AND t.heatNo = :heatNo AND t.sampleType = :sampleType AND t.strand = :strand AND t.sampleLot = :sampleLot")
    List<TestSampleAcptEntity> findByCompositeKey(
        @Param("sampleId") String sampleId,
        @Param("heatNo") String heatNo,
        @Param("sampleType") String sampleType,
        @Param("strand") Integer strand,
        @Param("sampleLot") String sampleLot
    );

    @Query("SELECT t FROM TestSampleAcptEntity t WHERE t.heatNo = :heatNo AND t.sampleLot = :sampleLot")
    List<TestSampleAcptEntity> findByHeatNoAndSampleLot(@Param("heatNo") String heatNo, @Param("sampleLot") String sampleLot);

    @Query(value = """
        SELECT
            rtsa.heat_number,
            rtsa.created_at,
            'acceptance' as type,
            rtsa.rolling_test_sample_id
        FROM rolling_test_sample_acceptance rtsa
        UNION ALL
        SELECT
            rs.heat_number,
            MIN(rs.create_date) as created_at,
            'retest' as type,
            MIN(rs.retest_sample_id) as rolling_test_sample_id
        FROM retest_sample rs
        GROUP BY rs.heat_number, rs.test_type, rs.sample_lot
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<Object[]> findAllTestSamples();

    // New method to get test samples filtered by current shift
    @Query(value = """
        SELECT
            rtsa.heat_number,
            rtsa.created_at,
            'acceptance' as type,
            rtsa.rolling_test_sample_id
        FROM rolling_test_sample_acceptance rtsa
        LEFT JOIN rolling_test_sample_acceptance_relation rtsar
            ON rtsa.rolling_test_sample_id = rtsar.rolling_test_sample_id
        LEFT JOIN rolling_duty rd
            ON rtsar.duty_id = rd.duty_id
        WHERE (rd.shift = :shift AND rd.date = :date)
           OR (rtsar.duty_id IS NULL AND DATE(rtsa.created_at) = :date)
        UNION ALL
        SELECT
            rs.heat_number,
            MIN(rs.create_date) as created_at,
            'retest' as type,
            MIN(rs.retest_sample_id) as rolling_test_sample_id
        FROM retest_sample rs
        LEFT JOIN rolling_duty rd
            ON rs.duty_id = rd.duty_id
        WHERE (rd.shift = :shift AND rd.date = :date)
           OR (rs.duty_id IS NULL AND DATE(rs.create_date) = :date)
        GROUP BY rs.heat_number, rs.test_type, rs.sample_lot
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<Object[]> findTestSamplesByShiftAndDate(@Param("shift") String shift, @Param("date") java.sql.Date date);

    @Query(value = """
        SELECT
            rtsar.duty_id,
            rtsa.rail_grade,
            rtsa.sample_type,
            rtsa.sample_id,
            rtsa.heat_number,
            rtsa.strand,
            rtsa.sample_lot,
            rtsa.chemical,
            rtsa.n2,
            rtsa.fwt_st,
            rtsa.mechanical,
            rtsa.sp,
            rtsa.ir,
            rtsa.o2,
            rtsa.fwt_hs,
            rtsa.fwt_st_sr,
            rtsa.tensile_foot,
            rtsa.micro,
            rtsa.decarb,
            rtsa.rsh,
            rtsa.ph,
            rtsa.tensile
        FROM rolling_test_sample_acceptance rtsa
        LEFT JOIN rolling_test_sample_acceptance_relation rtsar
            ON rtsa.rolling_test_sample_id = rtsar.rolling_test_sample_id
        WHERE rtsa.heat_number = :heatNumber
        LIMIT 1
        """, nativeQuery = true)
    List<Object[]> findTestSampleWithDutyId(@Param("heatNumber") String heatNumber);


}
