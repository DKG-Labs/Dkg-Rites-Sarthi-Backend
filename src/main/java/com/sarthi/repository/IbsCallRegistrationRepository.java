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

    @Query("""
        SELECT i.srNo 
        FROM IbsCallRegistration i 
        WHERE i.callNumber = :callNumber 
          AND i.srNo IS NOT NULL 
          AND TRIM(i.srNo) <> ''
        ORDER BY i.version DESC, i.id DESC
    """)
    List<String> findSrNoByCallNumber(@Param("callNumber") String callNumber);

    @Query("""
        SELECT i.callNumber, i.srNo 
        FROM IbsCallRegistration i 
        WHERE i.callNumber IN :callNumbers 
          AND i.srNo IS NOT NULL 
          AND TRIM(i.srNo) <> ''
        ORDER BY i.version DESC, i.id DESC
    """)
    List<Object[]> findSrNosByCallNumbers(@Param("callNumbers") java.util.Collection<String> callNumbers);

    @Query("""
        SELECT i.callNumber, i.status, i.reason, i.srNo 
        FROM IbsCallRegistration i 
        WHERE i.id IN (
            SELECT MAX(i2.id) 
            FROM IbsCallRegistration i2 
            WHERE i2.callNumber IN :callNumbers 
            GROUP BY i2.callNumber
        )
    """)
    List<Object[]> findLatestStatusByCallNumbers(@Param("callNumbers") java.util.Collection<String> callNumbers);
}

