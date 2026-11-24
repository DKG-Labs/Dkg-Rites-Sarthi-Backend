package com.sarthi.Sms.repository.vi;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sarthi.Sms.dto.vi.ViSubRailIdEntity;
import com.sarthi.Sms.dto.vi.ViSubRailIdEntity.PrimaryKey;
@Repository
public interface ViSubRailIdRepository extends JpaRepository<ViSubRailIdEntity, PrimaryKey> {
    List<ViSubRailIdEntity> findByRailId(String railId);
    ViSubRailIdEntity findBySubRailId(String subRailId);
}
