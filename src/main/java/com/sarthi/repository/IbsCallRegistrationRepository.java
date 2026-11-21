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
    @Query("SELECT i FROM IbsCallRegistration i WHERE i.callNumber = :callNumber ORDER BY i.id DESC")
    List<IbsCallRegistration> findByCallNumber(@Param("callNumber") String callNumber);

    List<IbsCallRegistration> findByCallNumberOrderByIdDesc(String callNumber);

    java.util.Optional<IbsCallRegistration> findFirstByCallNumberOrderByIdDesc(String callNumber);

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

    @Query("""
        SELECT i
        FROM IbsCallRegistration i
        WHERE i.id IN (
            SELECT MAX(i2.id) 
            FROM IbsCallRegistration i2 
            GROUP BY i2.callNumber
        )
        ORDER BY i.id DESC
    """)
    List<IbsCallRegistration> findCompletedCalls();

    @Query("""
        SELECT i
        FROM IbsCallRegistration i
        ORDER BY i.id DESC
    """)
    List<IbsCallRegistration> findAllCalls();

    // ---------- Completed calls screen: paging done in the database ----------
    // "Latest registration per call number" = MAX(id) per UPPER(TRIM(call_number)), same rule as findAllCalls() + first-seen.

    String LATEST_FILTERED = """
        FROM ibs_call_registration r
        WHERE r.id IN (
                SELECT MAX(x.id) FROM ibs_call_registration x
                WHERE x.call_number IS NOT NULL AND TRIM(x.call_number) <> ''
                GROUP BY UPPER(TRIM(x.call_number)))
          AND (:prefixRx IS NULL OR UPPER(TRIM(r.call_number)) REGEXP :prefixRx)
          AND (:status IS NULL OR UPPER(COALESCE(NULLIF(r.status, ''), 'SUCCESS')) = :status)
          AND (:search IS NULL OR r.call_number LIKE CONCAT('%', :search, '%') OR r.sr_no LIKE CONCAT('%', :search, '%'))
          AND (:fromTs IS NULL OR r.acknowledged_at >= :fromTs)
          AND (:toTs IS NULL OR r.acknowledged_at < :toTs)
        """;

    /** Ids of one page of completed calls, newest first. */
    @Query(value = "SELECT r.id " + LATEST_FILTERED + " ORDER BY r.id DESC LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Long> findCompletedPageIds(@Param("prefixRx") String prefixRx, @Param("status") String status,
                                    @Param("search") String search,
                                    @Param("fromTs") java.time.LocalDateTime fromTs, @Param("toTs") java.time.LocalDateTime toTs,
                                    @Param("limit") int limit, @Param("offset") int offset);

    /** Number of completed calls matching the filters. */
    @Query(value = "SELECT COUNT(*) " + LATEST_FILTERED, nativeQuery = true)
    long countCompleted(@Param("prefixRx") String prefixRx, @Param("status") String status,
                        @Param("search") String search,
                        @Param("fromTs") java.time.LocalDateTime fromTs, @Param("toTs") java.time.LocalDateTime toTs);

    /** Category cards: completed calls per call-number prefix (ER, EP, EF, SF, RPP, RPF, OTHER). */
    @Query(value = """
        SELECT CASE
                 WHEN UPPER(TRIM(r.call_number)) LIKE 'RPP%' THEN 'RPP'
                 WHEN UPPER(TRIM(r.call_number)) LIKE 'RPF%' THEN 'RPF'
                 WHEN UPPER(TRIM(r.call_number)) LIKE 'SF%'  THEN 'SF'
                 WHEN UPPER(TRIM(r.call_number)) LIKE 'ER%'  THEN 'ER'
                 WHEN UPPER(TRIM(r.call_number)) LIKE 'EP%'  THEN 'EP'
                 WHEN UPPER(TRIM(r.call_number)) LIKE 'EF%'  THEN 'EF'
                 ELSE 'OTHER' END AS category,
               COUNT(*) AS cnt
        FROM ibs_call_registration r
        WHERE r.id IN (
                SELECT MAX(x.id) FROM ibs_call_registration x
                WHERE x.call_number IS NOT NULL AND TRIM(x.call_number) <> ''
                GROUP BY UPPER(TRIM(x.call_number)))
        GROUP BY category
        """, nativeQuery = true)
    List<Object[]> countCompletedByCategory();
}

