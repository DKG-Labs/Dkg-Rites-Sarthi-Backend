package com.sarthi.Sms.repository.qct;

import com.sarthi.Sms.entity.qct.QctTestingMasterEntity;
import com.sarthi.Sms.entity.qct.QctTestingMasterId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QctTestingMasterRepository extends JpaRepository<QctTestingMasterEntity, QctTestingMasterId> {
}

