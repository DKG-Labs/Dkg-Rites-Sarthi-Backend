package com.sarthi.repository;

import com.sarthi.entity.ProcessInspectionDiscrepancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessInspectionDiscrepancyRepository extends JpaRepository<ProcessInspectionDiscrepancy, Long> {

    Optional<ProcessInspectionDiscrepancy> findByDiscrepancyNo(String discrepancyNo);

    @Query("SELECT p FROM ProcessInspectionDiscrepancy p WHERE p.discrepancyNo LIKE :prefix% ORDER BY p.discrepancyNo DESC LIMIT 1")
    Optional<ProcessInspectionDiscrepancy> findTopByDiscrepancyNoStartingWithOrderByDiscrepancyNoDesc(@Param("prefix") String prefix);

    @Query("SELECT p FROM ProcessInspectionDiscrepancy p " +
           "WHERE p.status IN ('CLOSED', 'WITHDRAWN') " +
           "AND (p.createdBy = :userId OR p.updatedBy = :userId OR p.vendorCode = (SELECT u.userName FROM UserMaster u WHERE u.userId = :userId)) " +
           "ORDER BY p.createdAt DESC")
    List<ProcessInspectionDiscrepancy> findCompletedDiscrepanciesByUserId(@Param("userId") Integer userId);

}
