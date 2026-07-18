package com.sarthi.repository;

import com.sarthi.entity.IBS.IbsCallRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface IbsCallRegistrationRepository
        extends JpaRepository<IbsCallRegistration, Long> {
    boolean existsByCallNumberAndStatus(String callNumber, String status);

    @Query("""
       SELECT i.callNumber
       FROM IbsCallRegistration i
       """)
    Set<String> findAllCallNumbers();

    @Query("SELECT COALESCE(MAX(i.version), 0) FROM IbsCallRegistration i WHERE i.callNumber = :callNumber")
    Integer getLatestVersion(@Param("callNumber") String callNumber);

    @Query("""
            SELECT i
            FROM IbsCallRegistration i
            WHERE i.status='SUCCESS'
            AND (
                i.billingStatus IS NULL
                OR i.billingStatus <> 'COMPLETED'
            )
           """)
    List<IbsCallRegistration> findPendingBillingCalls();
}
