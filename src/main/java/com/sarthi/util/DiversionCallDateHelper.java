package com.sarthi.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Persists and reads optional {@code call_date} on diversion request tables via JdbcTemplate
 * so JPA queries never fail with Unknown Column when {@code ddl-auto=none} is used prior to DDL rollout.
 */
@Slf4j
public final class DiversionCallDateHelper {

    private static final ConcurrentHashMap<String, Boolean> COLUMN_READY = new ConcurrentHashMap<>();

    private DiversionCallDateHelper() {}

    public static boolean ensureColumn(JdbcTemplate jdbc, String tableName) {
        if (jdbc == null || tableName == null) return false;
        return COLUMN_READY.computeIfAbsent(tableName, tbl -> {
            try {
                Integer cnt = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = 'call_date'",
                        Integer.class, tbl);
                if (cnt != null && cnt > 0) {
                    return true;
                }
                jdbc.execute("ALTER TABLE " + tbl + " ADD COLUMN call_date DATE NULL");
                return true;
            } catch (Exception e) {
                log.warn("call_date column not present on {} and auto-alter failed ({}): run database_migrations/V20261125_001__Add_Call_Date_To_Diversion_Request_Tables.sql",
                        tbl, e.getMessage());
                return false;
            }
        });
    }

    public static void saveCallDate(JdbcTemplate jdbc, String tableName, Long id, LocalDate callDate) {
        if (id == null || !ensureColumn(jdbc, tableName)) return;
        try {
            jdbc.update("UPDATE " + tableName + " SET call_date = ? WHERE id = ?",
                    callDate != null ? Date.valueOf(callDate) : null, id);
        } catch (Exception e) {
            log.warn("Could not save call_date on {} id={}: {}", tableName, id, e.getMessage());
        }
    }

    public static LocalDate loadCallDate(JdbcTemplate jdbc, String tableName, Long id) {
        if (id == null || !ensureColumn(jdbc, tableName)) return null;
        try {
            return jdbc.query("SELECT call_date FROM " + tableName + " WHERE id = ?",
                    rs -> rs.next() && rs.getDate(1) != null ? rs.getDate(1).toLocalDate() : null,
                    id);
        } catch (Exception e) {
            return null;
        }
    }

    public static Map<Long, LocalDate> loadCallDates(JdbcTemplate jdbc, String tableName, Collection<Long> ids) {
        if (ids == null || ids.isEmpty() || !ensureColumn(jdbc, tableName)) {
            return Collections.emptyMap();
        }
        String placeholders = ids.stream().map(x -> "?").collect(Collectors.joining(","));
        Object[] args = ids.toArray();
        Map<Long, LocalDate> result = new HashMap<>();
        try {
            jdbc.query("SELECT id, call_date FROM " + tableName + " WHERE id IN (" + placeholders + ")",
                    rs -> {
                        Date d = rs.getDate(2);
                        if (d != null) {
                            result.put(rs.getLong(1), d.toLocalDate());
                        }
                    },
                    args);
        } catch (Exception e) {
            log.warn("Could not load call_date map on {}: {}", tableName, e.getMessage());
        }
        return result;
    }
}
