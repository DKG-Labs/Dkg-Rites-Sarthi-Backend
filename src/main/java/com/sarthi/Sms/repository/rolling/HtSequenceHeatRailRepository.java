package com.sarthi.Sms.repository.rolling;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.rolling.HtSequenceHeatRailEntity;
import java.util.List;


@Repository
public interface HtSequenceHeatRailRepository extends JpaRepository<HtSequenceHeatRailEntity, String>{
    Optional<HtSequenceHeatRailEntity> findByRailId(String railId);
    List<HtSequenceHeatRailEntity> findByHeatNoOrderByCreatedAtDesc(String heatNo);
}
