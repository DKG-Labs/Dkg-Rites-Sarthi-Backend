package com.sarthi.Sms.repository.welding;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.welding.WeldingMacroEntity;
import com.sarthi.Sms.entity.welding.WeldingMacroId;
@Repository
public interface WeldingMacroRepository extends JpaRepository<WeldingMacroEntity, WeldingMacroId> {
    
    @Query(value = """
        SELECT 
            wid.date AS date,
            wid.rail_section AS railSection,
            wm.machine_number AS machineNumber,
            wm.head_l AS headL,
            wm.head_r AS headR,
            wm.web_l AS webL,
            wm.web_r AS webR,
            wm.foot_l AS footL,
            wm.foot_r AS footR,
            wm.remarks AS remark
        FROM welding_macro wm
        JOIN welding_inspection_duty wid ON wm.duty_id = wid.duty_id
        WHERE wm.joint_number = :jointNumber
        """, nativeQuery = true)
    List<Object[]> findWeldMacroDtlByJointNo(@Param("jointNumber") String jointNumber);
    
}
