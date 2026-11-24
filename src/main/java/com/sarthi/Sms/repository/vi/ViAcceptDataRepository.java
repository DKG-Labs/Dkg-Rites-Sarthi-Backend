package com.sarthi.Sms.repository.vi;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.visualInspection.ViAcceptDataEntity;
@Repository
public interface ViAcceptDataRepository extends JpaRepository<ViAcceptDataEntity, Integer>{
    List<ViAcceptDataEntity> findByRailId(String railId);

}
