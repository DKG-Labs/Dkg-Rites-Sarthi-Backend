package com.sarthi.udmcomplaint.repository;

import com.sarthi.udmcomplaint.entity.UdmComplaintStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UdmComplaintStageRepository extends JpaRepository<UdmComplaintStage, Long> {

    List<UdmComplaintStage> findByUdmRejectionIdOrderByCreatedAtAsc(Long udmRejectionId);
}
