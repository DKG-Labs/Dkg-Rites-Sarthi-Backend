-- =====================================================================
-- V20261111_007 : Clean up region_sbu_head rows saved with region = NULL
-- Cause: user save read "regionName" while the form sends "rio" (fixed in UserServiceImpl).
-- Review step 1 output first; then run step 2 for the rows you want to fix.
-- =====================================================================

-- 1) INSPECT (read only)
SELECT s.id, s.region_name, s.sbu_head_user_id, u.FULL_NAME, u.EMPLOYEE_CODE, u.Region AS user_rio
FROM region_sbu_head s
LEFT JOIN user_master u ON u.userid = s.sbu_head_user_id
ORDER BY s.region_name, s.id;

-- Duplicate regions would make the lookup fail (it expects one row per region):
SELECT region_name, COUNT(*) FROM region_sbu_head GROUP BY region_name HAVING COUNT(*) > 1;

-- 2) FIX — set the region from the user's RIO where it is missing,
--    but only if no other row already holds that RIO (1 region = 1 SBU Head).
START TRANSACTION;

UPDATE region_sbu_head s
JOIN user_master u ON u.userid = s.sbu_head_user_id
SET s.region_name = TRIM(u.Region)
WHERE s.id > 0   -- key column in WHERE: required by Workbench "safe update mode" (Error 1175)
  AND (s.region_name IS NULL OR TRIM(s.region_name) = '')
  AND u.Region IS NOT NULL AND TRIM(u.Region) <> ''
  AND NOT EXISTS (
      SELECT 1 FROM (SELECT region_name FROM region_sbu_head) x
      WHERE UPPER(TRIM(x.region_name)) = UPPER(TRIM(u.Region))
  );

-- Any row still without a region (user has no RIO) — review, then delete if not needed:
-- DELETE FROM region_sbu_head WHERE id > 0 AND (region_name IS NULL OR TRIM(region_name) = '');

COMMIT;

-- VERIFY
-- SELECT * FROM region_sbu_head ORDER BY region_name;
