-- DRY RUN of V20261127_002 (delete Kalimata Vyapaar disposal entry 932). Changes NOTHING:
-- everything runs inside a transaction that is rolled back at the end.
-- Run the whole script in one go (same Workbench tab / connection), then check the 4 result grids.
-- Expected: grid 1 = entry 932 ACTIVE, 22 pcs, 2026-11-22 · grid 2 = rows_updated 1 · grid 3 = 932 DELETED + 1 DELETE audit row
--           grid 4 (after rollback) = entry 932 ACTIVE again, no new audit row.

SET @entry_id = 932;
SET SQL_SAFE_UPDATES = 0;
SET autocommit = 0;
START TRANSACTION;

-- 1) before
SELECT id, poi_code, unit_line_shed_no, scrapping_date, shift, no_of_pieces_scrapped, status, scrap_verification_id
FROM ie_scrap_entry WHERE id = @entry_id;

-- 2) same statements as the real script
UPDATE ie_scrap_entry
SET status = 'DELETED', updated_by = 'DATA_FIX', updated_date = NOW()
WHERE id = @entry_id AND status = 'ACTIVE' AND scrapping_date = '2026-11-22' AND no_of_pieces_scrapped = 22;
SET @rows_updated = ROW_COUNT();

INSERT INTO ie_scrap_entry_audit (scrap_entry_id, action, field_changed, old_value, new_value, reason, action_by, action_at)
SELECT @entry_id, 'DELETE', NULL, 'ACTIVE', 'DELETED',
       'Data fix: disposal entry deleted on request (call EP-26111404, 22 Nos, 22.11.2026)', 'DATA_FIX', NOW()
FROM DUAL WHERE @rows_updated = 1;

SELECT @rows_updated AS rows_updated;

-- 3) what it would look like
SELECT e.id, e.status, e.updated_by, a.action, a.reason
FROM ie_scrap_entry e
LEFT JOIN ie_scrap_entry_audit a ON a.scrap_entry_id = e.id AND a.action = 'DELETE' AND a.action_by = 'DATA_FIX'
WHERE e.id = @entry_id;

-- undo everything
ROLLBACK;
SET autocommit = 1;
SET SQL_SAFE_UPDATES = 1;

-- 4) after rollback: must be back to ACTIVE, no DATA_FIX audit row
SELECT e.id, e.status,
       (SELECT COUNT(*) FROM ie_scrap_entry_audit a WHERE a.scrap_entry_id = e.id AND a.action_by = 'DATA_FIX') AS data_fix_audit_rows
FROM ie_scrap_entry e WHERE e.id = @entry_id;
