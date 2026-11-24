package com.sarthi.Sms.repository.rolling;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.rolling.HtSequenceHeatEntity;
@Repository
public interface HtSequenceHeatRepository extends JpaRepository<HtSequenceHeatEntity, String>{

    Optional<HtSequenceHeatEntity> findByHtSequenceBatchCompleteFalse();
}
