package com.sarthi.udmcomplaint.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sarthi.udmcomplaint.UdmComplaintConstants.*;

/**
 * Server-side, role-scoped, paginated UDM complaint list (udm_rejection LEFT JOIN udm_complaint).
 * Values are always bound as parameters; only fixed SQL fragments (from constants) are concatenated.
 */
@Repository
public class UdmComplaintListRepository {

    public static final String VIEW_CC = "cc";
    public static final String VIEW_CM = "cm";
    public static final String VIEW_SBU = "sbu";
    public static final String VIEW_RB = "rb";
    public static final String VIEW_BOARD = "board";

    /** view = role screen, mine = "Pending with Me / Assigned to Me" scope, closed = Closed tab. */
    public record Filter(String view, boolean mine, boolean closed, Integer userId, String q) { }

    /** Counts for the tab badges (search applied). */
    public record Counts(long pending, long closed, long mine, long all) { }

    @PersistenceContext
    private EntityManager em;

    private static final String FROM = " FROM udm_rejection r LEFT JOIN udm_complaint c ON c.udm_rejection_id = r.id ";
    private static final String IS_CLOSED = "c.status = '" + ST_CLOSED + "'";
    private static final String NOT_CLOSED = "(c.status IS NULL OR c.status <> '" + ST_CLOSED + "')";
    /** Still with the Complaint Cell: never actioned (no udm_complaint row) or status PENDING_WITH_COMPLAINT_CELL. */
    private static final String PENDING_WITH_CC = "(c.id IS NULL OR c.status = '" + ST_PENDING_CC + "')";

    /** Ids of one page, newest first. */
    @SuppressWarnings("unchecked")
    public List<Long> findIds(Filter f, int offset, int limit) {
        Map<String, Object> p = new HashMap<>();
        String sql = "SELECT r.id" + FROM + "WHERE " + where(f, p, true)
                + " ORDER BY r.fetched_date DESC, r.rejection_date DESC, r.id DESC";
        Query query = em.createNativeQuery(sql);
        p.forEach(query::setParameter);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return ((List<Object>) query.getResultList()).stream().map(o -> ((Number) o).longValue()).toList();
    }

    /** Rows matching the current scope + tab + search (for paging). */
    public long count(Filter f) {
        Map<String, Object> p = new HashMap<>();
        Query query = em.createNativeQuery("SELECT COUNT(*)" + FROM + "WHERE " + where(f, p, true));
        p.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }

    /** All badge counts in one query: pending / closed in scope, "mine" to-do, all rows (search applied). */
    public Counts counts(Filter f) {
        Map<String, Object> p = new HashMap<>();
        String scope = f.mine() ? mineCondition(f.view(), p, f.userId()) : "1=1";
        String todo = todoCondition(f.view(), p, f.userId());
        String sql = "SELECT"
                + " SUM(CASE WHEN " + scope + " AND " + NOT_CLOSED + " THEN 1 ELSE 0 END),"
                + " SUM(CASE WHEN " + scope + " AND " + IS_CLOSED + " THEN 1 ELSE 0 END),"
                + " SUM(CASE WHEN " + todo + " THEN 1 ELSE 0 END),"
                + " COUNT(*)"
                + FROM + "WHERE " + searchCondition(f.q(), p);
        Query query = em.createNativeQuery(sql);
        p.forEach(query::setParameter);
        Object[] row = (Object[]) query.getSingleResult();
        return new Counts(num(row[0]), num(row[1]), num(row[2]), num(row[3]));
    }

    // ------------------------------------------------------------------ SQL fragments

    private String where(Filter f, Map<String, Object> p, boolean withTab) {
        StringBuilder w = new StringBuilder(searchCondition(f.q(), p));
        if (f.mine()) w.append(" AND ").append(mineCondition(f.view(), p, f.userId()));
        if (withTab) w.append(" AND ").append(f.closed() ? IS_CLOSED : NOT_CLOSED);
        return w.toString();
    }

    /**
     * "Mine" per role:
     *  CC  — not forwarded yet, or closed directly by the Complaint Cell
     *  CM  — assigned to me (open + closed)
     *  SBU — waiting for SBU review, or closed after an SBU review
     *  RB  — waiting for Railway Board, or closed after a Railway Board review
     */
    private String mineCondition(String view, Map<String, Object> p, Integer userId) {
        switch (view) {
            case VIEW_CC -> {
                // CC: not forwarded yet (no complaint row = never actioned), or closed directly by the Complaint Cell
                return "(" + PENDING_WITH_CC + " OR (" + IS_CLOSED
                        + " AND EXISTS (SELECT 1 FROM udm_complaint_workflow_transition t WHERE t.complaint_id = c.id AND t.action = '"
                        + TR_CLOSE + "')))";
            }
            case VIEW_CM -> {
                p.put("uid", userId == null ? -1 : userId);
                return "c.assigned_cm_user_id = :uid";
            }
            case VIEW_SBU -> {
                return "(c.status IN ('" + ST_CAPA_SUBMITTED + "', '" + ST_RETURNED_TO_SBU + "') OR (" + IS_CLOSED
                        + " AND EXISTS (SELECT 1 FROM udm_complaint_stage s WHERE s.complaint_id = c.id AND s.stage = '"
                        + STAGE_SBU_REVIEW + "')))";
            }
            case VIEW_RB -> {
                return "(c.status = '" + ST_SENT_TO_RB + "' OR (" + IS_CLOSED
                        + " AND EXISTS (SELECT 1 FROM udm_complaint_stage s WHERE s.complaint_id = c.id AND s.stage = '"
                        + STAGE_RB_REVIEW + "')))";
            }
            default -> {
                return "1=1";
            }
        }
    }

    /** Badge count of the "mine" tab: CM = assigned to me; SBU / RB = waiting for my review now. */
    private String todoCondition(String view, Map<String, Object> p, Integer userId) {
        switch (view) {
            case VIEW_CC -> {
                return PENDING_WITH_CC;
            }
            case VIEW_CM -> {
                p.put("uid", userId == null ? -1 : userId);
                return "c.assigned_cm_user_id = :uid";
            }
            case VIEW_SBU -> {
                return "c.status IN ('" + ST_CAPA_SUBMITTED + "', '" + ST_RETURNED_TO_SBU + "')";
            }
            case VIEW_RB -> {
                return "c.status = '" + ST_SENT_TO_RB + "'";
            }
            default -> {
                return "1=0";
            }
        }
    }

    private String searchCondition(String q, Map<String, Object> p) {
        if (q == null || q.isBlank()) return "1=1";
        p.put("q", "%" + q.trim().toLowerCase().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        return "(LOWER(r.rejection_number) LIKE :q OR LOWER(r.po_number) LIKE :q OR LOWER(r.pl_number) LIKE :q"
                + " OR LOWER(r.ic_number) LIKE :q OR LOWER(r.vendor_code) LIKE :q OR LOWER(r.vendor_name) LIKE :q"
                + " OR LOWER(r.consignee) LIKE :q OR LOWER(r.type_of_rejection) LIKE :q"
                + " OR LOWER(c.assigned_cm_name) LIKE :q OR LOWER(c.assigned_rio) LIKE :q)";
    }

    private static long num(Object o) { return o == null ? 0L : ((Number) o).longValue(); }
}
