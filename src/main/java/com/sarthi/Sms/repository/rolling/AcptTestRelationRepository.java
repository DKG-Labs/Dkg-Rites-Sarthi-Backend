package com.sarthi.Sms.repository.rolling;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.rolling.AcptTestRelationEntity;
import com.sarthi.Sms.entity.rolling.AcptTestRelationId;
@Repository
public interface AcptTestRelationRepository extends JpaRepository<AcptTestRelationEntity, AcptTestRelationId>{
    Optional<AcptTestRelationEntity> findByAtri(AcptTestRelationId atri);
}
