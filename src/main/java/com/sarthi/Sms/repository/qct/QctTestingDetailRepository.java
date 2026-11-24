package com.sarthi.Sms.repository.qct;

import com.sarthi.Sms.entity.qct.QctTestingDetailEntity;
import com.sarthi.Sms.entity.qct.QctTestingDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QctTestingDetailRepository extends JpaRepository<QctTestingDetailEntity, QctTestingDetailId> {
}
