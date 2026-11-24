package com.sarthi.Sms.repository.qct;

import com.sarthi.Sms.entity.qct.QctDetailEntity;
import com.sarthi.Sms.entity.qct.QctDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QctDetailRepository extends JpaRepository<QctDetailEntity, QctDetailId> {

    List<QctDetailEntity> findByDutyId(String dutyId);

}
