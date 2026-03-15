package com.sarthi.Sleeper.repository.FinalInspectionRepository;


import com.sarthi.Sleeper.entity.FinalInspection.MorSampleDeclaration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MorSampleRepository extends JpaRepository<MorSampleDeclaration, Long> {
}
