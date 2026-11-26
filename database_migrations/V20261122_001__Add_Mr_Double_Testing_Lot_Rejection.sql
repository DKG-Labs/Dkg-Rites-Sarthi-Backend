-- MR (Moment of Resistance) test: a failure in double testing (02 samples, the retest after a first-test "Retest")
-- rejects the entire lot: returned to the vendor and every sleeper shown as Rejected, through the same
-- production_declaration.lot_status used by the water cube test.
--
-- Plain statements only (no dynamic SQL), so they can be run all at once or one by one.

-- 0. moment_of_resistance.mr_test_type already exists on some databases (e.g. UAT: VARCHAR(50) DEFAULT 'Fresh').
--    Check first:   SHOW COLUMNS FROM moment_of_resistance LIKE 'mr_test_type';
--    Only when that returns NO row, remove the "-- " in front of the next line and run it:
-- ALTER TABLE moment_of_resistance ADD COLUMN mr_test_type VARCHAR(20) NULL;
--    mr_test_type holds the value the MR page sends (Fresh / Retest); the retest value is
--    sleeper.mr.retest-test-type in application.properties.

-- 1. MR sample declarations: the lot (production batch) the samples were taken from.
ALTER TABLE moment_of_resistance
    ADD COLUMN production_declaration_id BIGINT NULL;

CREATE INDEX idx_moment_of_resistance_pd ON moment_of_resistance (production_declaration_id);

-- 2. MR test results: the sample declaration each result belongs to (filled for new results only).
ALTER TABLE moment_of_resistance_test
    ADD COLUMN moment_of_resistance_id BIGINT NULL;

-- 3. lot_rejected_by_test_id can now point to a water cube test or an MR test, so the test type is stored with it.
--    Values mirror com.sarthi.Sleeper.entity.ProductionDeclaration.LotRejectionTestType (stored as the enum name).
ALTER TABLE production_declaration
    ADD COLUMN lot_rejected_by_test_type VARCHAR(20) NULL;

-- 4. Data backfill. These UPDATEs filter on non-key columns, which MySQL Workbench's safe update mode blocks,
--    so it is switched off here and restored at the end.
SET @old_sql_safe_updates = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- Every lot rejected so far was rejected by a water cube test.
UPDATE production_declaration
SET lot_rejected_by_test_type = 'WATER_CUBE'
WHERE lot_status = 'REJECTED'
  AND lot_rejected_by_test_id IS NOT NULL
  AND lot_rejected_by_test_type IS NULL;

-- Link existing MR declarations to their lot (no lot is rejected here), so retests already declared keep working.
-- The MR page writes the lot id into remarks as "[DeclId: N]"; it is used only when that production declaration
-- exists and has the same batch number. The id is cast only after it is checked to be numeric.
UPDATE moment_of_resistance m
JOIN production_declaration pd
  ON pd.id = CASE
               WHEN TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(m.remarks, '[DeclId:', -1), ']', 1)) REGEXP '^[0-9]+$'
               THEN CAST(TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(m.remarks, '[DeclId:', -1), ']', 1)) AS UNSIGNED)
             END
SET m.production_declaration_id = pd.id
WHERE m.production_declaration_id IS NULL
  AND m.remarks LIKE '%[DeclId:%'
  AND CONVERT(UPPER(TRIM(pd.batch_number)) USING utf8mb4) COLLATE utf8mb4_unicode_ci
    = CONVERT(UPPER(TRIM(m.batch_number)) USING utf8mb4) COLLATE utf8mb4_unicode_ci;

-- The MR page writes "Declared for MR <type> Testing (...)" into remarks when a sample is declared.
-- The application never stored mr_test_type before this change, so an existing 'Fresh' is only the column default
-- (where that default exists) and is corrected for samples declared for retest.
UPDATE moment_of_resistance
SET mr_test_type = 'Retest'
WHERE (mr_test_type IS NULL OR mr_test_type = 'Fresh')
  AND remarks LIKE '%Declared for MR Retest Testing%';

UPDATE moment_of_resistance
SET mr_test_type = 'Fresh'
WHERE mr_test_type IS NULL
  AND remarks LIKE '%Declared for MR Fresh Testing%';

SET SQL_SAFE_UPDATES = @old_sql_safe_updates;

-- 5. Check: each of these must return 1 row.
-- SHOW COLUMNS FROM moment_of_resistance LIKE 'mr_test_type';
-- SHOW COLUMNS FROM moment_of_resistance LIKE 'production_declaration_id';
-- SHOW COLUMNS FROM moment_of_resistance_test LIKE 'moment_of_resistance_id';
-- SHOW COLUMNS FROM production_declaration LIKE 'lot_rejected_by_test_type';

-- Review only (old results are NOT rejected automatically): lots that already have a "Fail" in double testing.
-- SELECT m.production_declaration_id, m.batch_number, m.sleeper_no, t.id AS test_id, t.test_result, t.created_date
-- FROM moment_of_resistance m
-- JOIN moment_of_resistance_test t
--   ON UPPER(TRIM(t.batch_number)) = UPPER(TRIM(m.batch_number))
--  AND UPPER(TRIM(t.sleeper_no)) = UPPER(TRIM(m.sleeper_no))
--  AND t.created_date >= m.created_date
-- WHERE m.mr_test_type = 'Retest'
--   AND UPPER(TRIM(t.test_result)) = 'FAIL';
