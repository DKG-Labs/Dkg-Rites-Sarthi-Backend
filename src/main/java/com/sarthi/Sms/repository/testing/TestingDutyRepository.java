package com.sarthi.Sms.repository.testing;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.testing.TestingDutyEntity;
@Repository
public interface TestingDutyRepository extends JpaRepository<TestingDutyEntity, String>{

    @Query(value="SELECT duty_id FROM testing_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    String checkDutyStatus(@Param("userId") String userId);

    Optional<TestingDutyEntity> findByDutyId(String dutyId);

    @Query(value="SELECT * FROM testing_duty WHERE user_id=:userId AND end_time IS NULL", nativeQuery = true)
    Optional<TestingDutyEntity> getOngoingDutyDtls(@Param("userId") String userId);

    // @Query
    // ("SELECT new com.sarthi.Sms.dto.testing.PendingTestSampleDtlResDto(" +
    //        "rtsa.heatNo, rtsa.strand, rd.mill, rtsa.railGrade, rtsa.sampleType, rtsa.sampleLot, " +
    //        "rtsa.sampleId, rtsar.testsMarked) " +
    //        "FROM TestSampleAcptEntity rtsa " +
    //        "JOIN AcptTestRelationEntity rtsar ON rtsa.id = rtsar.testSampleAcpt.id " +
    //        "JOIN RollingDutyEntity rd ON rtsar.duty.id = rd.dutyId " +
    //        "WHERE " +
    //        "(rtsa.chemical = TRUE AND (rtsa.chemicalTestStatus = 'RETEST' OR rtsa.chemicalTestStatus IS NULL)) OR " +
    //        "(rtsa.n2 = TRUE AND (rtsa.n2TestStatus = 'RETEST' OR rtsa.n2TestStatus IS NULL)) OR " +
    //        "(rtsa.fwtSt = TRUE AND (rtsa.fwtStTestStatus = 'RETEST' OR rtsa.fwtStTestStatus IS NULL)) OR " +
    //        "(rtsa.mechanical = TRUE AND (rtsa.mechanicalTestStatus = 'RETEST' OR rtsa.mechanicalTestStatus IS NULL)) OR " +
    //        "(rtsa.sp = TRUE AND (rtsa.spTestStatus = 'RETEST' OR rtsa.spTestStatus IS NULL)) OR " +
    //        "(rtsa.ir = TRUE AND (rtsa.irTestStatus = 'RETEST' OR rtsa.irTestStatus IS NULL)) OR " +
    //        "(rtsa.o2 = TRUE AND (rtsa.o2TestStatus = 'RETEST' OR rtsa.o2TestStatus IS NULL)) OR " +
    //        "(rtsa.fwtHs = TRUE AND (rtsa.fwtHsTestStatus = 'RETEST' OR rtsa.fwtHsTestStatus IS NULL)) OR " +
    //        "(rtsa.fwtStSr = TRUE AND (rtsa.fwtStSrTestStatus = 'RETEST' OR rtsa.fwtStSrTestStatus IS NULL)) OR " +
    //        "(rtsa.tensileFoot = TRUE AND (rtsa.tensileFootTestStatus = 'RETEST' OR rtsa.tensileFootTestStatus IS NULL)) OR " +
    //        "(rtsa.micro = TRUE AND (rtsa.microTestStatus = 'RETEST' OR rtsa.microTestStatus IS NULL)) OR " +
    //        "(rtsa.decarb = TRUE AND (rtsa.decarbTestStatus = 'RETEST' OR rtsa.decarbTestStatus IS NULL)) OR " +
    //        "(rtsa.rsh = TRUE AND (rtsa.rshTestStatus = 'RETEST' OR rtsa.rshTestStatus IS NULL)) OR " +
    //        "(rtsa.ph = TRUE AND (rtsa.phTestStatus = 'RETEST' OR rtsa.phTestStatus IS NULL)) OR " +
    //        "(rtsa.tensile = TRUE AND (rtsa.tensileTestStatus = 'RETEST' OR rtsa.tensileTestStatus IS NULL))")
    // List<PendingTestSampleDtlResDto> findPendingTestSampleDtls();

    // @Query(value = """
    //     SELECT 
    //         rtsa.heat_number,
    //         rtsa.strand,
    //         rd.mill,
    //         rtsa.rail_grade,
    //         rtsa.sample_type,
    //         rtsa.sample_lot,
    //         rtsa.sample_id,
    //         rtsar.tests_marked
    //     FROM rolling_test_sample_acceptance rtsa
    //     JOIN rolling_test_sample_acceptance_relation rtsar 
    //         ON rtsa.rolling_test_sample_id = rtsar.rolling_test_sample_id
    //     JOIN rolling_duty rd 
    //         ON rtsar.duty_id = rd.duty_id
    //     WHERE (
    //         (rtsa.chemical = TRUE AND (rtsa.chemical_test_status = 'RETEST' OR rtsa.chemical_test_status IS NULL)) OR
    //         (rtsa.n2 = TRUE AND (rtsa.n2_test_status = 'RETEST' OR rtsa.n2_test_status IS NULL)) OR
    //         (rtsa.fwt_st = TRUE AND (rtsa.fwt_st_test_status = 'RETEST' OR rtsa.fwt_st_test_status IS NULL)) OR
    //         (rtsa.mechanical = TRUE AND (rtsa.mechanical_test_status = 'RETEST' OR rtsa.mechanical_test_status IS NULL)) OR
    //         (rtsa.sp = TRUE AND (rtsa.sp_test_status = 'RETEST' OR rtsa.sp_test_status IS NULL)) OR
    //         (rtsa.ir = TRUE AND (rtsa.ir_test_status = 'RETEST' OR rtsa.ir_test_status IS NULL)) OR
    //         (rtsa.o2 = TRUE AND (rtsa.o2_test_status = 'RETEST' OR rtsa.o2_test_status IS NULL)) OR
    //         (rtsa.fwt_hs = TRUE AND (rtsa.fwt_hs_test_status = 'RETEST' OR rtsa.fwt_hs_test_status IS NULL)) OR
    //         (rtsa.fwt_st_sr = TRUE AND (rtsa.fwt_st_sr_test_status = 'RETEST' OR rtsa.fwt_st_sr_test_status IS NULL)) OR
    //         (rtsa.tensile_foot = TRUE AND (rtsa.tensile_foot_test_status = 'RETEST' OR rtsa.tensile_foot_test_status IS NULL)) OR
    //         (rtsa.micro = TRUE AND (rtsa.micro_test_status = 'RETEST' OR rtsa.micro_test_status IS NULL)) OR
    //         (rtsa.decarb = TRUE AND (rtsa.decarb_test_status = 'RETEST' OR rtsa.decarb_test_status IS NULL)) OR
    //         (rtsa.rsh = TRUE AND (rtsa.rsh_test_status = 'RETEST' OR rtsa.rsh_test_status IS NULL)) OR
    //         (rtsa.sp = TRUE AND (rtsa.sp_test_status = 'RETEST' OR rtsa.sp_test_status IS NULL)) OR
    //         (rtsa.ph = TRUE AND (rtsa.ph_test_status = 'RETEST' OR rtsa.ph_test_status IS NULL)) OR
    //         (rtsa.tensile = TRUE AND (rtsa.tensile_test_status = 'RETEST' OR rtsa.tensile_test_status IS NULL))
    //     )
    //     """, nativeQuery = true)
    //     List<Object[]> findPendingTestSampleDtls();

    @Query(value = """
        SELECT
            rtsa.heat_number,
            rtsa.strand,
            rd.mill,
            COALESCE(rtsa.rail_grade, rd.rail_grade) as rail_grade,
            rtsa.sample_type,
            rtsa.sample_lot,
            COALESCE(rtsa.sample_id, CONCAT('AUTO-', rtsa.rolling_test_sample_id)) as sample_id,
            CONCAT_WS(',',
                CASE WHEN rtsa.chemical = TRUE THEN 'CHEMICAL' END,
                CASE WHEN rtsa.n2 = TRUE THEN 'N2' END,
                CASE WHEN rtsa.fwt_st = TRUE THEN 'FWT (ST)' END,
                CASE WHEN rtsa.fwt_hs = TRUE THEN 'FWT (HS)' END,
                CASE WHEN rtsa.fwt_st_sr = TRUE THEN 'FWT (ST) - SR' END,
                CASE WHEN rtsa.mechanical = TRUE THEN 'MECHANICAL' END,
                CASE WHEN rtsa.sp = TRUE THEN 'SP' END,
                CASE WHEN rtsa.ir = TRUE THEN 'IR' END,
                CASE WHEN rtsa.o2 = TRUE THEN 'O2' END,
                CASE WHEN rtsa.tensile_foot = TRUE THEN 'TENSILE FOOT' END,
                CASE WHEN rtsa.micro = TRUE THEN 'MICRO' END,
                CASE WHEN rtsa.decarb = TRUE THEN 'DECARB' END,
                CASE WHEN rtsa.rsh = TRUE THEN 'RSH' END,
                CASE WHEN rtsa.ph = TRUE THEN 'PH' END,
                CASE WHEN rtsa.tensile = TRUE THEN 'TENSILE' END
            ) as tests_marked,
            JSON_OBJECT(
                'CHEMICAL', (
                    SELECT chemical_ladle_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'N2', (
                    SELECT n2_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'FWT', (
                    SELECT fwt_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'MECHANICAL', (
                    SELECT macro_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'SP', (
                    SELECT sp_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'IR', (
                    SELECT ir_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'O2', (
                    SELECT o2_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'TENSILE_FOOT', (
                    SELECT tensile_foot_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'MICRO', (
                    SELECT micro_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'DECARB', (
                    SELECT decarb_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'TENSILE', (
                    SELECT tensile_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'RSH', (
                    SELECT rsh_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'PH', (
                    SELECT ph_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                )
            ) as test_statuses
        FROM rolling_test_sample_acceptance rtsa
        JOIN rolling_test_sample_acceptance_relation rtsar
            ON rtsa.rolling_test_sample_id = rtsar.rolling_test_sample_id
        JOIN rolling_duty rd
            ON rtsar.duty_id = rd.duty_id
        WHERE
            (rtsa.chemical = TRUE OR rtsa.n2 = TRUE OR rtsa.fwt_st = TRUE OR
             rtsa.mechanical = TRUE OR rtsa.sp = TRUE OR rtsa.ir = TRUE OR
             rtsa.o2 = TRUE OR rtsa.fwt_hs = TRUE OR rtsa.fwt_st_sr = TRUE OR
             rtsa.tensile_foot = TRUE OR rtsa.micro = TRUE OR rtsa.decarb = TRUE OR
             rtsa.rsh = TRUE OR rtsa.ph = TRUE OR rtsa.tensile = TRUE)
        HAVING tests_marked IS NOT NULL AND tests_marked != ''
        ORDER BY rtsa.created_at DESC
        LIMIT 100
        """, nativeQuery = true)
    List<Object[]> findPendingTestSampleDtls();

    @Query(value = """
        SELECT
            rtsa.heat_number,
            rtsa.strand,
            rd.mill,
            COALESCE(rtsa.rail_grade, rd.rail_grade) as rail_grade,
            rtsa.sample_type,
            rtsa.sample_lot,
            rtsa.sample_id,
            CONCAT_WS(',',
                CASE WHEN rtsa.chemical = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.chemical_ladle_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'CHEMICAL' END,
                CASE WHEN rtsa.n2 = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.n2_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'N2' END,
                CASE WHEN rtsa.fwt_st = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.fwt_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'FWT (ST)' END,
                CASE WHEN rtsa.fwt_hs = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.fwt_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'FWT (HS)' END,
                CASE WHEN rtsa.fwt_st_sr = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.fwt_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'FWT (ST) - SR' END,
                CASE WHEN rtsa.mechanical = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.macro_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'MECHANICAL' END,
                CASE WHEN rtsa.sp = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.sp_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'SP' END,
                CASE WHEN rtsa.ir = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.ir_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'IR' END,
                CASE WHEN rtsa.o2 = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.o2_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'O2' END,
                CASE WHEN rtsa.tensile_foot = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.tensile_foot_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'TENSILE FOOT' END,
                CASE WHEN rtsa.micro = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.micro_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'MICRO' END,
                CASE WHEN rtsa.decarb = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.decarb_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'DECARB' END,
                CASE WHEN rtsa.rsh = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.rsh_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'RSH' END,
                CASE WHEN rtsa.ph = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.ph_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'PH' END,
                CASE WHEN rtsa.tensile = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.tensile_status IS NOT NULL
                      AND ct.duty_id != ?1
                ) THEN 'TENSILE' END
            ) as tests_marked,
            JSON_OBJECT(
                'CHEMICAL', (
                    SELECT chemical_ladle_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'N2', (
                    SELECT n2_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'FWT', (
                    SELECT fwt_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'MECHANICAL', (
                    SELECT macro_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'SP', (
                    SELECT sp_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'IR', (
                    SELECT ir_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'O2', (
                    SELECT o2_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'TENSILE_FOOT', (
                    SELECT tensile_foot_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'MICRO', (
                    SELECT micro_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'DECARB', (
                    SELECT decarb_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'TENSILE', (
                    SELECT tensile_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'RSH', (
                    SELECT rsh_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                ),
                'PH', (
                    SELECT ph_status
                    FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                    AND ct.strand_number = rtsa.strand
                    LIMIT 1
                )
            ) as test_statuses
        FROM rolling_test_sample_acceptance rtsa
        JOIN rolling_test_sample_acceptance_relation rtsar
            ON rtsa.rolling_test_sample_id = rtsar.rolling_test_sample_id
        JOIN rolling_duty rd
            ON rtsar.duty_id = rd.duty_id
        WHERE
            rd.user_id = ?1
            AND rd.end_time IS NULL
            AND (
                (rtsa.chemical = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.chemical_ladle_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.n2 = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.n2_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.fwt_st = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.fwt_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.mechanical = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.macro_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.sp = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.sp_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.ir = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.ir_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.o2 = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.o2_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.fwt_hs = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.fwt_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.fwt_st_sr = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.fwt_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.tensile_foot = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.tensile_foot_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.micro = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.micro_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.decarb = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.decarb_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.rsh = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.rsh_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.ph = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.ph_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                )) OR
                (rtsa.tensile = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.tensile_status IS NOT NULL
                      AND ct.duty_id != rd.duty_id
                ))
            )
        ORDER BY rtsa.created_at DESC
        LIMIT 100
        """, nativeQuery = true)
    List<Object[]> findPendingTestSampleDtlsByUserId(String userId);

    @Query(value = """
        SELECT
            rtsa.heat_number,
            rtsa.strand,
            rd.mill,
            COALESCE(rtsa.rail_grade, rd.rail_grade) as rail_grade,
            rtsa.sample_type,
            rtsa.sample_lot,
            COALESCE(rtsa.sample_id, CONCAT('AUTO-', rtsa.rolling_test_sample_id)) as sample_id,
            CONCAT_WS(',',
                CASE WHEN rtsa.chemical = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.chemical_ladle_status IS NOT NULL
                ) THEN 'CHEMICAL' END,
                CASE WHEN rtsa.n2 = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.n2_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'N2' END,
                CASE WHEN rtsa.fwt_st = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.fwt_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'FWT (ST)' END,
                CASE WHEN rtsa.fwt_hs = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.fwt_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'FWT (HS)' END,
                CASE WHEN rtsa.fwt_st_sr = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.fwt_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'FWT (ST) - SR' END,
                CASE WHEN rtsa.mechanical = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.macro_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'MECHANICAL' END,
                CASE WHEN rtsa.sp = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.sp_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'SP' END,
                CASE WHEN rtsa.ir = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.ir_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'IR' END,
                CASE WHEN rtsa.o2 = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.o2_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'O2' END,
                CASE WHEN rtsa.tensile_foot = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.tensile_foot_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'TENSILE FOOT' END,
                CASE WHEN rtsa.micro = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.micro_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'MICRO' END,
                CASE WHEN rtsa.decarb = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.decarb_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'DECARB' END,
                CASE WHEN rtsa.rsh = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.rsh_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'RSH' END,
                CASE WHEN rtsa.ph = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.ph_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'PH' END,
                CASE WHEN rtsa.tensile = TRUE AND NOT EXISTS (
                    SELECT 1 FROM completed_tests ct
                    WHERE ct.heat_number = rtsa.heat_number
                      AND ct.strand_number = rtsa.strand
                      AND ct.tensile_status IS NOT NULL
                      AND ct.duty_id = ?1
                ) THEN 'TENSILE' END
            ) as tests_marked,
            'N/A' as test_statuses
        FROM rolling_test_sample_acceptance rtsa
        JOIN rolling_test_sample_acceptance_relation rtsar
            ON rtsa.rolling_test_sample_id = rtsar.rolling_test_sample_id
        JOIN rolling_duty rd
            ON rtsar.duty_id = rd.duty_id
        JOIN testing_duty td
            ON td.user_id = ?1 AND td.end_time IS NULL
        WHERE
            (rtsa.chemical = TRUE OR rtsa.n2 = TRUE OR rtsa.fwt_st = TRUE OR
             rtsa.mechanical = TRUE OR rtsa.sp = TRUE OR rtsa.ir = TRUE OR
             rtsa.o2 = TRUE OR rtsa.fwt_hs = TRUE OR rtsa.fwt_st_sr = TRUE OR
             rtsa.tensile_foot = TRUE OR rtsa.micro = TRUE OR rtsa.decarb = TRUE OR
             rtsa.rsh = TRUE OR rtsa.ph = TRUE OR rtsa.tensile = TRUE)
        HAVING tests_marked IS NOT NULL AND tests_marked != ''
        ORDER BY rtsa.created_at DESC
        LIMIT 100
        """, nativeQuery = true)
    List<Object[]> findPendingTestSampleDtlsForCurrentDuty(String userId, String dutyId);

    @Query(value = """
        SELECT
            rtsa.heat_number,
            rtsa.strand,
            rd.mill,
            COALESCE(rtsa.rail_grade, rd.rail_grade) as rail_grade,
            rtsa.sample_type,
            rtsa.sample_lot,
            COALESCE(rtsa.sample_id, CONCAT('AUTO-', rtsa.rolling_test_sample_id)) as sample_id,
            CONCAT_WS(',',
                CASE WHEN rtsa.chemical = TRUE AND (
                    -- Show if: (not completed anywhere) OR (completed in current duty only)
                    NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.chemical_ladle_status IS NOT NULL
                          AND ct.duty_id != ?1
                    )
                ) THEN 'CHEMICAL' END,
                CASE WHEN rtsa.n2 = TRUE AND (
                    -- Show if: not completed in different duty
                    NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.n2_status IS NOT NULL
                          AND ct.duty_id != ?1
                    )
                ) THEN 'N2' END,
                CASE WHEN rtsa.fwt_st = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.fwt_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'FWT (ST)' END,
                CASE WHEN rtsa.fwt_hs = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.fwt_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'FWT (HS)' END,
                CASE WHEN rtsa.fwt_st_sr = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.fwt_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'FWT (ST) - SR' END,
                CASE WHEN rtsa.mechanical = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.macro_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'MECHANICAL' END,
                CASE WHEN rtsa.sp = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.sp_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'SP' END,
                CASE WHEN rtsa.ir = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.ir_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'IR' END,
                CASE WHEN rtsa.o2 = TRUE AND (
                    NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.o2_status IS NOT NULL
                    ) OR EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.o2_status IS NOT NULL
                          AND ct.duty_id = ?1
                    )
                ) THEN 'O2' END,
                CASE WHEN rtsa.tensile_foot = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.tensile_foot_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'TENSILE FOOT' END,
                CASE WHEN rtsa.micro = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.micro_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'MICRO' END,
                CASE WHEN rtsa.decarb = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.decarb_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'DECARB' END,
                CASE WHEN rtsa.rsh = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.rsh_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'RSH' END,
                CASE WHEN rtsa.ph = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.ph_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'PH' END,
                CASE WHEN rtsa.tensile = TRUE AND NOT EXISTS (
                        SELECT 1 FROM completed_tests ct
                        WHERE ct.heat_number = rtsa.heat_number
                          AND ct.strand_number = rtsa.strand
                          AND ct.tensile_status IS NOT NULL
                          AND ct.duty_id != ?1
                    ) THEN 'TENSILE' END
            ) as tests_marked,
            JSON_OBJECT(
                'CHEMICAL', (SELECT chemical_ladle_status FROM completed_tests ct
                           WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'N2', (SELECT n2_status FROM completed_tests ct
                      WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'FWT', (SELECT fwt_status FROM completed_tests ct
                       WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'MECHANICAL', (SELECT macro_status FROM completed_tests ct
                              WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'SP', (SELECT sp_status FROM completed_tests ct
                      WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'IR', (SELECT ir_status FROM completed_tests ct
                      WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'O2', (SELECT o2_status FROM completed_tests ct
                      WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'TENSILE_FOOT', (SELECT tensile_foot_status FROM completed_tests ct
                                WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'MICRO', (SELECT micro_status FROM completed_tests ct
                         WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'DECARB', (SELECT decarb_status FROM completed_tests ct
                          WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'TENSILE', (SELECT tensile_status FROM completed_tests ct
                           WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'RSH', (SELECT rsh_status FROM completed_tests ct
                       WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1),
                'PH', (SELECT ph_status FROM completed_tests ct
                      WHERE ct.heat_number = rtsa.heat_number AND ct.strand_number = rtsa.strand LIMIT 1)
            ) as test_statuses
        FROM rolling_test_sample_acceptance rtsa
        JOIN rolling_test_sample_acceptance_relation rtsar
            ON rtsa.rolling_test_sample_id = rtsar.rolling_test_sample_id
        JOIN rolling_duty rd
            ON rtsar.duty_id = rd.duty_id
        WHERE
            (rtsa.chemical = TRUE OR rtsa.n2 = TRUE OR rtsa.fwt_st = TRUE OR
             rtsa.mechanical = TRUE OR rtsa.sp = TRUE OR rtsa.ir = TRUE OR
             rtsa.o2 = TRUE OR rtsa.fwt_hs = TRUE OR rtsa.fwt_st_sr = TRUE OR
             rtsa.tensile_foot = TRUE OR rtsa.micro = TRUE OR rtsa.decarb = TRUE OR
             rtsa.rsh = TRUE OR rtsa.ph = TRUE OR rtsa.tensile = TRUE)
        HAVING tests_marked IS NOT NULL AND tests_marked != ''
        ORDER BY rtsa.created_at DESC
        LIMIT 100
        """, nativeQuery = true)
    List<Object[]> findPendingTestSampleDtlsWithDutyFilter(String userId, String currentDutyId);

}
