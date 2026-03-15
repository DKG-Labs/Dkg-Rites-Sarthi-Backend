package com.sarthi.Sleeper.repository;

import com.sarthi.Sleeper.entity.AdmixtureTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmixtureTestRepository extends JpaRepository<AdmixtureTest, Long> {
    List<AdmixtureTest> findByConsignmentNo(String consignmentNo);
}
