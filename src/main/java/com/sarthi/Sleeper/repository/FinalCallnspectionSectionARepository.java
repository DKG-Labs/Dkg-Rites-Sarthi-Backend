package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.FInalCall.FinalCallnspectionSectionA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinalCallnspectionSectionARepository extends JpaRepository<FinalCallnspectionSectionA,Long> {
    Optional<FinalCallnspectionSectionA> findByCallNo(String callNo);
}
