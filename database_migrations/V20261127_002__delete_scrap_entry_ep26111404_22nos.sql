-- PROD data fix: delete the IE disposal (scrap) entry of 22 Nos dated 22.11.2026 (shift A), IE 104554.
-- Asked: call EP-26111404, Kalimata, 22.11.2026, 22 Nos. Scrap entries are kept per unit (POI), not per call:
--   id 967  POI20  KIIPL, UNIT-II (Kalimata Ispat)          SC-26112332  <- unit of call EP-26111404
--   id 932  POI28  M/s KALIMATA VYAPAAR PVT LTD - Barjora    SC-26112204  <- "Kalimata Vyapar"
-- (id 968, POI28, SC-26112333 is already DELETED.)
--
-- Same as the app's Delete: status -> DELETED + audit row. Row and images are kept (can be undone).
-- Set @entry_id to the one to delete (967 or 932) and run the whole script.

SET @entry_id = 932;   -- M/s Kalimata Vyapaar (POI28), SC-26112204 — confirmed by user
SET SQL_SAFE_UPDATES = 0;

-- 1) check: must show exactly one ACTIVE row, 22 pieces, 2026-11-22
SELECT id, poi_code, unit_line_shed_no, scrapping_date, shift, no_of_pieces_scrapped, status, scrap_verification_id
FROM ie_scrap_entry
WHERE id = @entry_id;

-- 2) soft delete (only if still ACTIVE with the expected date / qty)
UPDATE ie_scrap_entry
SET status = 'DELETED', updated_by = 'DATA_FIX', updated_date = NOW()
WHERE id = @entry_id
  AND status = 'ACTIVE'
  AND scrapping_date = '2026-11-22'
  AND no_of_pieces_scrapped = 22;
SET @rows_updated = ROW_COUNT();

-- 3) audit trail (only when step 2 changed the row)
INSERT INTO ie_scrap_entry_audit (scrap_entry_id, action, field_changed, old_value, new_value, reason, action_by, action_at)
SELECT @entry_id, 'DELETE', NULL, 'ACTIVE', 'DELETED',
       'Data fix: disposal entry deleted on request (call EP-26111404, 22 Nos, 22.11.2026)', 'DATA_FIX', NOW()
FROM DUAL
WHERE @rows_updated = 1;

SET SQL_SAFE_UPDATES = 1;

-- 4) verify
SELECT e.id, e.status, e.updated_by, e.updated_date, a.action, a.reason, a.action_at
FROM ie_scrap_entry e
LEFT JOIN ie_scrap_entry_audit a ON a.scrap_entry_id = e.id AND a.action = 'DELETE'
WHERE e.id = @entry_id;
