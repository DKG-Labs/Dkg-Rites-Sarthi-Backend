-- ============================================================================
-- One-time: copy IE -> CM links that exist ONLY in user_product_cm_mapping into
-- user_master.controlling_manager_id (the code no longer reads user_product_cm_mapping).
-- Run manually (local / QA / UAT / PROD), AFTER deploying the backend change.
-- Only fills IEs whose controlling_manager_id is empty; never overwrites an existing CM.
-- The old table is NOT dropped or changed (kept for audit / rollback).
-- ============================================================================

-- 1) PREVIEW (read-only): what will be copied
SELECT ie.userid, ie.employee_code AS ie_code, ie.full_name AS ie_name,
       upcm.product_type, cm.userid AS cm_userid, cm.employee_code AS cm_code, cm.full_name AS cm_name
FROM user_product_cm_mapping upcm
JOIN user_master ie ON ie.employee_code = upcm.user_employee_code
JOIN user_master cm ON cm.employee_code = upcm.cm_employee_code
WHERE ie.controlling_manager_id IS NULL
ORDER BY ie.employee_code;

-- 1b) IEs with DIFFERENT CMs in the old table for different products (only the lowest mapping id is copied).
--     Review these by hand in Admin -> IE Mapping after running step 2.
SELECT upcm.user_employee_code, COUNT(DISTINCT upcm.cm_employee_code) AS cm_count,
       GROUP_CONCAT(DISTINCT CONCAT(upcm.product_type, ':', upcm.cm_employee_code)) AS mappings
FROM user_product_cm_mapping upcm
GROUP BY upcm.user_employee_code
HAVING COUNT(DISTINCT upcm.cm_employee_code) > 1;

-- 2) COPY (run after checking step 1). One CM per IE: the earliest mapping row.
UPDATE user_master ie
JOIN (
    SELECT u.user_employee_code, u.cm_employee_code
    FROM user_product_cm_mapping u
    JOIN (SELECT user_employee_code, MIN(id) AS first_id
          FROM user_product_cm_mapping GROUP BY user_employee_code) f
      ON f.first_id = u.id
) src ON src.user_employee_code = ie.employee_code
JOIN user_master cm ON cm.employee_code = src.cm_employee_code
SET ie.controlling_manager_id = cm.userid
WHERE ie.controlling_manager_id IS NULL
  AND ie.userid > 0;   -- satisfies MySQL Workbench safe-update mode

-- 3) VERIFY (read-only): should return 0 rows
SELECT ie.employee_code, ie.full_name
FROM user_product_cm_mapping upcm
JOIN user_master ie ON ie.employee_code = upcm.user_employee_code
WHERE ie.controlling_manager_id IS NULL;
