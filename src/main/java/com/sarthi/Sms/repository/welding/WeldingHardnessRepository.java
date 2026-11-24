package com.sarthi.Sms.repository.welding;

import com.sarthi.Sms.entity.welding.WeldingHardnessEntity;
import com.sarthi.Sms.entity.welding.WeldingHardnessKey;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeldingHardnessRepository extends JpaRepository<WeldingHardnessEntity, WeldingHardnessKey> {

    @Query(value = """
        SELECT 
            wid.date AS date,
            wid.rail_section AS railSection,
            wh.parent_hardness_l AS parentHardnessL,
            wh.parent_hardness_r AS parentHardnessR,
            wh.haz_hardness_l AS hazHardnessL,
            wh.haz_hardness_r AS hazHardnessR,
            wh.deviation_l AS deviationL,
            wh.deviation_r AS deviationR,
            wh.remark AS remark
        FROM welding_hardness wh
        JOIN welding_inspection_duty wid ON wh.duty_id = wid.duty_id
        WHERE wh.joint_number = :jointNumber
        """, nativeQuery = true)
    List<Object[]> findHardnessDtlByJointNo(@Param("jointNumber") String jointNumber);
}
