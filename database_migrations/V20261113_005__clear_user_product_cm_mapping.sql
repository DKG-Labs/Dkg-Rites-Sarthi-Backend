-- ============================================================================
-- Clear user_product_cm_mapping (no longer used by the code: IE -> CM now lives in
-- user_master.controlling_manager_id, set via Admin -> IE Mapping).
-- Run manually, AFTER V20261113_003__copy_cm_links_to_user_master.sql and the backend deploy.
-- The table itself is kept (empty); a full backup copy is kept for rollback.
-- ============================================================================

-- 1) CHECK (read-only): IEs that are in the old table but have NO CM in user_master yet.
--    Must return 0 rows before you continue. If not, run V20261113_003 first.
SELECT upcm.user_employee_code, upcm.product_type, upcm.cm_employee_code
FROM user_product_cm_mapping upcm
JOIN user_master ie ON ie.employee_code = upcm.user_employee_code
WHERE ie.controlling_manager_id IS NULL;

-- 2) BACKUP: full copy of the table (rollback = INSERT back from this table)
CREATE TABLE IF NOT EXISTS user_product_cm_mapping_bkp_20261113 AS
SELECT * FROM user_product_cm_mapping;

-- Confirm the backup has the same row count as the original before continuing
SELECT (SELECT COUNT(*) FROM user_product_cm_mapping)             AS original_rows,
       (SELECT COUNT(*) FROM user_product_cm_mapping_bkp_20261113) AS backup_rows;

-- 3) CLEAR (run only if step 1 returned 0 rows and the two counts above match)
DELETE FROM user_product_cm_mapping
WHERE id > 0;   -- WHERE on the key keeps MySQL Workbench safe-update mode happy

-- 4) VERIFY: should be 0
SELECT COUNT(*) AS remaining_rows FROM user_product_cm_mapping;

-- ----------------------------------------------------------------------------
-- ROLLBACK (only if needed):
-- INSERT INTO user_product_cm_mapping SELECT * FROM user_product_cm_mapping_bkp_20261113;
-- Once everything is confirmed fine (e.g. after a week), the backup can be dropped:
-- DROP TABLE user_product_cm_mapping_bkp_20261113;
-- ----------------------------------------------------------------------------
