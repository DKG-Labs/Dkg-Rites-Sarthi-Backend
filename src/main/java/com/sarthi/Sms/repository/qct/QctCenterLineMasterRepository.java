package com.sarthi.Sms.repository.qct;
import com.sarthi.Sms.entity.qct.QctCenterLineMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QctCenterLineMasterRepository extends JpaRepository<QctCenterLineMasterEntity, Integer> {
    
}
