package com.sarthi.Sms.repository.qct;

import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.qct.QctFatigueDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface QctFatigueDetailRepository extends JpaRepository<QctFatigueDetailEntity, Integer> {
    
}
