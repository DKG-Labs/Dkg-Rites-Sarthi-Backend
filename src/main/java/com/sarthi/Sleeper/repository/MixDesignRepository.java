package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.MixDesign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MixDesignRepository extends JpaRepository<MixDesign, Long> {


    List<MixDesign> findByIdIn(List<Long> ids);

    @Query(value = """
    SELECT md.* 
    FROM mix_design md
    JOIN sleeper_workflow_transaction swt
      ON md.id = swt.request_id
    WHERE swt.module_id = :moduleId
      AND swt.status = 'COMPLETED'
""", nativeQuery = true)
    List<MixDesign> findApprovedMixDesigns(@Param("moduleId") Long moduleId);

}
