package com.sarthi.Sleeper.repository.FinalInspectionRepository;


import com.sarthi.Sleeper.entity.FinalInspection.MorSampleDeclaration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MorSampleRepository extends JpaRepository<MorSampleDeclaration, Long> {

    @Query("SELECT m FROM MorSampleDeclaration m WHERE NOT EXISTS (" +
            "SELECT 1 FROM MorTestResult t WHERE t.morSample.id = m.id)")
    List<MorSampleDeclaration> findAllNotTested();
}
