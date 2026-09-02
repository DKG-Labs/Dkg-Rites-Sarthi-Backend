package com.sarthi.Sleeper.repository.FinalInspectionRepository;

import com.sarthi.Sleeper.entity.FinalInspection.SleeperInspectionCompleteDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SleeperInspectionCompleteDetailsRepository extends JpaRepository<SleeperInspectionCompleteDetails, Long> {

    Optional<SleeperInspectionCompleteDetails> findByCallNo(String callNo);

    Optional<SleeperInspectionCompleteDetails> findFirstByCallNoOrderByCreatedOnDesc(String callNo);

    @Query(value = "SELECT CERTIFICATE_NO FROM SLEEPER_INSPECTION_COMPLETE_DETAILS WHERE CALL_NO = :callNo ORDER BY id DESC LIMIT 1", nativeQuery = true)
    String findCertificateNoByCallNo(@Param("callNo") String callNo);
}
