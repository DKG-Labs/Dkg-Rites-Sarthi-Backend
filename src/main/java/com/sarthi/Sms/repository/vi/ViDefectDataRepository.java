package com.sarthi.Sms.repository.vi;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.entity.visualInspection.ViDefectDataEntity;
@Repository
public interface ViDefectDataRepository extends JpaRepository<ViDefectDataEntity, Integer>{
    List<ViDefectDataEntity> findByRailId(String railId);
}
