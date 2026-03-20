package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.AdmixtureTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;
import org.springframework.data.repository.query.Param;

@Repository
public interface AdmixtureTestRepository extends JpaRepository<AdmixtureTest, Long> {
    List<AdmixtureTest> findByConsignmentNo(String consignmentNo);
    Optional<AdmixtureTest> findByRequestId(@Param("requestId") Long requestId);
}
