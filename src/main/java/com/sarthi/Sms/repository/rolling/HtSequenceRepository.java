package com.sarthi.Sms.repository.rolling;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.sarthi.Sms.entity.rolling.HtSequenceEntity;
@Repository
public interface HtSequenceRepository extends JpaRepository<HtSequenceEntity, String> {
    List<HtSequenceEntity> findByBatchOpenTrue();
    
    @Query(value = """
        SELECT 
            hs.rail_id,
            hs.bloom_quality,
            hs.ht_status,
            -- hs.created_at,
            DATE_FORMAT(hs.created_at, '%Y-%m-%d %H:%i:%s') AS created_at,
            CONCAT_WS(', ',
                CASE WHEN rtsa.chemical THEN 'Chemical' END,
                CASE WHEN rtsa.n2 THEN 'N2' END,
                CASE WHEN rtsa.fwt_st THEN 'FWT (St.)' END,
                CASE WHEN rtsa.mechanical THEN 'Mechanical' END,
                CASE WHEN rtsa.sp THEN 'SP' END,
                CASE WHEN rtsa.ir THEN 'IR' END,
                CASE WHEN rtsa.o2 THEN 'O2' END,
                CASE WHEN rtsa.fwt_hs THEN 'FWT (HS)' END,
                CASE WHEN rtsa.fwt_st_sr THEN 'FWT (St. SR)' END,
                CASE WHEN rtsa.tensile_foot THEN 'Tensile Foot' END,
                CASE WHEN rtsa.micro THEN 'Mic' END,
                CASE WHEN rtsa.decarb THEN 'Decarb' END,
                CASE WHEN rtsa.rsh THEN 'RSH' END,
                CASE WHEN rtsa.ph THEN 'PH' END,
                CASE WHEN rtsa.tensile THEN 'Tensile' END
            ) as tests_marked
        FROM ht_sequence hs
        LEFT JOIN rolling_test_sample_acceptance rtsa ON hs.rail_id = rtsa.sample_id
        WHERE hs.batch_open = true
        """, nativeQuery = true)
    List<Object[]> findOpenBatchDetailsWithTests();
}