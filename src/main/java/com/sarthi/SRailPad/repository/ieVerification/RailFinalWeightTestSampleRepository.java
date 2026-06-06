package com.sarthi.SRailPad.repository.ieVerification;

import com.sarthi.SRailPad.entity.ieVerification.RailFinalWeightTestSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RailFinalWeightTestSampleRepository extends JpaRepository<RailFinalWeightTestSample, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM RailFinalWeightTestSample s WHERE s.railFinalWeightTest.id = :weightTestId")
    void deleteByWeightTestId(@Param("weightTestId") Long weightTestId);
}
