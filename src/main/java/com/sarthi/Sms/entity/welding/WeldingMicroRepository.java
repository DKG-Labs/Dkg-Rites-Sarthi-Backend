package com.sarthi.Sms.entity.welding;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeldingMicroRepository extends JpaRepository<WeldingMicroEntity, WeldingMicroId> {
    @Query(value = """
        SELECT 
            wid.date AS date,
            wid.rail_section AS railSection,
            wm.is_micro_free AS isMicroFree,
            wm.is_astm_coarser AS isAstmCoarser,
            wm.remarks AS remark
        FROM welding_micro wm
        JOIN welding_inspection_duty wid ON wm.duty_id = wid.duty_id
        WHERE wm.joint_number = :jointNumber
        """, nativeQuery = true)
    List<Object[]> findWeldMicroDtl(@Param("jointNumber") String jointNumber);
}
