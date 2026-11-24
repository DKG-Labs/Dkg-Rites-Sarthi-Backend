package com.sarthi.Sms.repository.welding;

import com.sarthi.Sms.entity.welding.WeldingTltEntity;
import com.sarthi.Sms.entity.welding.WeldingTltKey;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeldingTltRepository extends JpaRepository<WeldingTltEntity, WeldingTltKey> {
    @Query(value = """
    SELECT 
        widuty.rail_section,
        wt.machine_number,
        widuty.date,
        wt.load_applied,
        wt.deflection,
        wt.span,
        CASE 
            WHEN wt.is_broken = TRUE THEN 'YES' 
            ELSE 'NO' 
        END AS isBroken,
        wt.remark
    FROM welding_tlt wt
    JOIN welding_inspection_duty widuty ON wt.duty_id = widuty.duty_id
    WHERE wt.joint_number = :jointNumber
    """, nativeQuery = true)
    List<Object[]> findTltDtlByJointNo(@Param("jointNumber") String jointNumber);

}
