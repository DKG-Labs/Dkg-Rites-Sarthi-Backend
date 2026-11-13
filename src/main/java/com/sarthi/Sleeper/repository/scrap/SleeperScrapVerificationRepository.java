package com.sarthi.Sleeper.repository.scrap;

import com.sarthi.Sleeper.entity.scrap.SleeperScrapVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * Sleeper scrap verifications plus the live rejection queries that feed the Rejection Basket.
 * Rejections are joined only on numeric ids (no cross-table string joins, which hit collation mismatches);
 * plant ids are compared without the leading ':'. An empty :plantId means all plants.
 */
@Repository
public interface SleeperScrapVerificationRepository extends JpaRepository<SleeperScrapVerification, Long> {

    // ---------- Scrap verifications ----------

    @Query(value = """
            SELECT * FROM sleeper_scrap_verification
            WHERE status = 'ACTIVE' AND (:plantId = '' OR plant_id = :plantId)
            ORDER BY scrapping_date, created_date
            """, nativeQuery = true)
    List<SleeperScrapVerification> findActive(@Param("plantId") String plantId);

    @Query(value = """
            SELECT MAX(CAST(SUBSTRING_INDEX(verification_no, '-', -1) AS UNSIGNED))
            FROM sleeper_scrap_verification WHERE verification_no LIKE :prefix
            """, nativeQuery = true)
    Long maxSequenceForPrefix(@Param("prefix") String prefix);

    /** Locks the plant row so two scrap verifications for the same plant are serialised */
    @Query(value = """
            SELECT id FROM vendor_plant WHERE REPLACE(plant_id, ':', '') = :plantId FOR UPDATE
            """, nativeQuery = true)
    List<Long> lockPlant(@Param("plantId") String plantId);

    // ---------- Demoulding (in-process) rejections: one row per defective sleeper ----------

    /**
     * 0 defect id, 1 bench/gang no, 2 sequence no, 3 sleeper no, 4 visual reason, 5 dim reason, 6 defect created,
     * 7 inspection id, 8 plant id, 9 vendor code, 10 batch no, 11 bench no, 12 casting date, 13 inspection date,
     * 14 sleeper type, 15 shift, 16 line/shed, 17 created by, 18 IE name, 19 inspection created
     */
    @Query(value = """
            SELECT d.id, d.bench_gang_no, d.sequence_no, d.sleeper_no, d.visual_reason, d.dim_reason, d.created_date,
                   i.id, i.plant_id, i.vendor_code, i.batch_no, i.bench_no, i.casting_date, i.inspection_date,
                   i.sleeper_type, i.shift, i.line_shed_no, i.created_by, um.full_name, i.created_date
            FROM demoulding_defective_sleepers d
            JOIN demoulding_inspection i ON i.id = d.inspection_id
            LEFT JOIN user_master um ON um.userid = i.created_by
            WHERE (TRIM(COALESCE(d.visual_reason, '')) <> '' OR TRIM(COALESCE(d.dim_reason, '')) <> '')
              AND i.plant_id IS NOT NULL
              AND (:plantId = '' OR REPLACE(i.plant_id, ':', '') = :plantId)
            """, nativeQuery = true)
    List<Object[]> findDemouldingRejections(@Param("plantId") String plantId);

    // ---------- Final inspection rejections (Main IE): one row per rejected sleeper ----------

    /**
     * The final-inspection screen saves by POST, which inserts a fresh ie_batch_summary row (with its full
     * rejected / ET lists) every time, so older snapshots pile up. Only the latest save of each call + batch is
     * the IE's current decision; earlier ones would count sleepers the IE later accepted.
     * Uncorrelated on purpose: MySQL materialises the latest-id set once (a correlated MAX per row took minutes).
     */
    String LATEST_BATCH_SAVE = """
            (b.call_no IS NULL OR b.id IN (SELECT MAX(b2.id) FROM ie_batch_summary b2
                                           WHERE b2.call_no IS NOT NULL GROUP BY b2.call_no, b2.batch_no))""";

    /**
     * 0 row id, 1 sleeper code, 2 reason, 3 type, 4 batch summary id, 5 plant id, 6 vendor code, 7 batch no,
     * 8 call no, 9 date casted, 10 shift, 11 created by, 12 IE name, 13 created date.
     * The plant of a final rejection is its inspection call's plant (like ERC), so for one plant this returns the
     * batches saved under that plant OR belonging to one of its calls (:plantCalls, never empty); the service
     * then settles each row's plant from the call.
     */
    @Query(value = """
            SELECT r.id, r.sleeper_code, r.reason, r.type, b.id, b.plant_id, b.vendor_code, b.batch_no,
                   b.call_no, b.date_casted, b.shift, b.created_by, um.full_name, b.created_date
            FROM final_call_rejected_sleepers r
            JOIN ie_batch_summary b ON b.id = r.batch_id
            LEFT JOIN user_master um ON um.userid = b.created_by
            WHERE r.sleeper_code IS NOT NULL AND TRIM(r.sleeper_code) NOT IN ('', '0')
              AND (:plantId = '' OR REPLACE(b.plant_id, ':', '') = :plantId OR b.call_no IN (:plantCalls))
              AND """ + LATEST_BATCH_SAVE + """
            UNION ALL
            SELECT r.id, r.sleeper_code, r.reason, NULL, b.id, b.plant_id, b.vendor_code, b.batch_no,
                   b.call_no, b.date_casted, b.shift, b.created_by, um.full_name, b.created_date
            FROM final_inspection_rejections r
            JOIN ie_batch_summary b ON b.id = r.batch_id
            LEFT JOIN user_master um ON um.userid = b.created_by
            WHERE r.sleeper_code IS NOT NULL AND TRIM(r.sleeper_code) NOT IN ('', '0')
              AND (:plantId = '' OR REPLACE(b.plant_id, ':', '') = :plantId OR b.call_no IN (:plantCalls))
              AND """ + LATEST_BATCH_SAVE + """
            """, nativeQuery = true)
    List<Object[]> findFinalRejections(@Param("plantId") String plantId,
                                       @Param("plantCalls") Collection<String> plantCalls);

    // ---------- Process IE final inspection forms (Visual Check / Critical / Non-Critical Dimensions) ----------

    /**
     * Sleepers currently marked REJECTED on the Visual Check & Measurement (module 1), Critical Dimensions (2) and
     * Non-Critical Dimensions (3) forms. Only active results count (a sleeper reset to pending is inactive / OK).
     * 0 result id, 1 sleeper no, 2 rejection reason, 3 module id, 4 plant id, 5 vendor code, 6 batch no,
     * 7 casting date, 8 sleeper type, 9 shift, 10 test date, 11 created by, 12 IE name, 13 created/updated date
     */
    @Query(value = """
            SELECT itr.id, itr.sleeper_no, itr.rejection_reason, COALESCE(itr.module_id, ith.module_id),
                   pd.plant_id, pd.vendor_code, pd.batch_number, pd.casting_date,
                   ith.sleeper_type, ith.shift, ith.test_date, COALESCE(itr.updated_by, ith.created_by),
                   um.full_name, COALESCE(itr.updated_date, ith.created_date)
            FROM inspection_test_result itr
            JOIN inspection_test_header ith ON ith.id = itr.test_header_id
            JOIN production_declaration pd ON pd.id = ith.batch_id
            LEFT JOIN user_master um ON um.userid = COALESCE(itr.updated_by, ith.created_by)
            WHERE UPPER(TRIM(itr.result)) = 'REJECTED'
              AND COALESCE(itr.active, 1) = 1
              AND itr.sleeper_no IS NOT NULL AND TRIM(itr.sleeper_no) <> ''
              AND pd.plant_id IS NOT NULL
              AND (:plantId = '' OR REPLACE(pd.plant_id, ':', '') = :plantId)
            """, nativeQuery = true)
    List<Object[]> findProcessFinalRejections(@Param("plantId") String plantId);

    // ---------- Destructive testing: only MR (Moment of Resistance) failures count (client decision) ----------

    /**
     * Failed MR tests. sleeper_no / bench_number may hold comma-separated lists (one test covers 1-2 sleepers).
     * 0 id, 1 batch no, 2 sleeper type, 3 casting date (text), 4 date of testing (text), 5 bench no(s),
     * 6 sleeper no(s), 7 plant id, 8 shift, 9 created by, 10 IE name, 11 created date
     */
    @Query(value = """
            SELECT t.id, t.batch_number, t.sleeper_type, t.casting_date, t.date_of_testing, t.bench_number,
                   t.sleeper_no, t.plant_id, t.shift, t.created_by, um.full_name, t.created_date
            FROM moment_of_resistance_test t
            LEFT JOIN user_master um ON um.userid = t.created_by
            WHERE UPPER(TRIM(t.test_result)) IN ('FAIL', 'FAILED', 'REJECTED')
              AND t.sleeper_no IS NOT NULL AND TRIM(t.sleeper_no) <> ''
              AND t.plant_id IS NOT NULL
              AND (:plantId = '' OR REPLACE(t.plant_id, ':', '') = :plantId)
            """, nativeQuery = true)
    List<Object[]> findMrFailures(@Param("plantId") String plantId);

    // ---------- Epoxy-treated sleepers: not rejections (client decision) ----------

    /** Process-stage epoxy treatment records. 0 plant id, 1 batch no, 2 sleeper no */
    @Query(value = """
            SELECT et.plant_id, et.batch_number, d.sleeper_no
            FROM et_sleeper_details d
            JOIN et_epoxy_treated_sleeper et ON et.id = d.et_id
            WHERE d.sleeper_no IS NOT NULL AND et.plant_id IS NOT NULL
              AND (:plantId = '' OR REPLACE(et.plant_id, ':', '') = :plantId)
            """, nativeQuery = true)
    List<Object[]> findEpoxyTreated(@Param("plantId") String plantId);

    /** Sleepers the Main IE marked for epoxy treatment at final inspection. 0 batch plant id, 1 batch no, 2 sleeper code, 3 call no */
    @Query(value = """
            SELECT b.plant_id, b.batch_no, s.sleeper_code, b.call_no
            FROM final_call_et_sleeper s
            JOIN ie_batch_summary b ON b.id = s.batch_id
            WHERE s.sleeper_code IS NOT NULL AND TRIM(s.sleeper_code) NOT IN ('', '0')
              AND (:plantId = '' OR REPLACE(b.plant_id, ':', '') = :plantId OR b.call_no IN (:plantCalls))
              AND """ + LATEST_BATCH_SAVE + """
            """, nativeQuery = true)
    List<Object[]> findFinalEpoxySleepers(@Param("plantId") String plantId,
                                          @Param("plantCalls") Collection<String> plantCalls);

    /** Call numbers of one plant's sleeper inspection calls */
    @Query(value = """
            SELECT call_no FROM sleeper_inspection_call
            WHERE call_no IS NOT NULL AND REPLACE(plant_id, ':', '') = :plantId
            """, nativeQuery = true)
    List<String> findCallNosByPlant(@Param("plantId") String plantId);

    /** 0 call number, 1 plant id, 2 sleeper type: the call is the source of truth for a final rejection's plant */
    @Query(value = """
            SELECT call_no, plant_id, sleeper_type FROM sleeper_inspection_call WHERE call_no IN (:callNos)
            """, nativeQuery = true)
    List<Object[]> findCallInfo(@Param("callNos") Collection<String> callNos);

    // ---------- Production (vendor production declarations) ----------

    /** 0 plant id (no ':'), 1 sleepers cast since :since. An empty :plantId means all plants. */
    @Query(value = """
            SELECT REPLACE(plant_id, ':', ''), COALESCE(SUM(total_casted_sleepers), 0)
            FROM production_declaration
            WHERE casting_date >= :since AND plant_id IS NOT NULL
              AND (:plantId = '' OR REPLACE(plant_id, ':', '') = :plantId)
            GROUP BY REPLACE(plant_id, ':', '')
            """, nativeQuery = true)
    List<Object[]> sumProductionSince(@Param("since") LocalDate since, @Param("plantId") String plantId);

    /** 0 call number, 1 sleeper type, 2 date of inspection */
    @Query(value = """
            SELECT call_number, sleeper_type, date_of_inspection
            FROM sleeper_final_result WHERE call_number IN (:callNos)
            """, nativeQuery = true)
    List<Object[]> findFinalResultInfo(@Param("callNos") Collection<String> callNos);
}
