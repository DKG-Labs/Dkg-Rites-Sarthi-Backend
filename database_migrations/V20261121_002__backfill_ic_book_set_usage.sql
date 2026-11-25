-- Backfill ic_book_set_usage with Book No + Set No already used on existing ICs
-- (ERC RM / Process / Final, Sleeper Final, Rail Pad Process / Final).
-- Rule: for each Book No + Set No the EARLIEST IC keeps it; every later IC is then blocked by the app.
-- Rows the app already wrote (since V20261121_001) are included in the comparison, so an older IC wins over them.
-- Run once. Step 4 lists Book/Set already shared by more than one IC (data to review, nothing is changed there).

-- 1. Every Book/Set used on any IC, with when and by whom
DROP TEMPORARY TABLE IF EXISTS tmp_bookset_all;
CREATE TEMPORARY TABLE tmp_bookset_all (
  book_no       VARCHAR(50)  COLLATE utf8mb4_unicode_ci,
  set_no        VARCHAR(20)  COLLATE utf8mb4_unicode_ci,
  ic_number     VARCHAR(150) COLLATE utf8mb4_unicode_ci,
  product_stage VARCHAR(30),
  user_id       VARCHAR(50),
  used_at       DATETIME,
  -- same IC key rule as the app: "R/CALL/IE" -> CALL, anything else -> whole value (computed, no UPDATE needed)
  ic_key        VARCHAR(150) COLLATE utf8mb4_unicode_ci AS (UPPER(TRIM(CASE WHEN ic_number LIKE '%/%/%'
                    THEN SUBSTRING_INDEX(SUBSTRING_INDEX(ic_number, '/', 2), '/', -1)
                    ELSE ic_number END))) STORED,
  KEY (book_no, set_no)
);

INSERT INTO tmp_bookset_all (book_no, set_no, ic_number, product_stage, user_id, used_at)
SELECT UPPER(TRIM(b)), UPPER(TRIM(s)), TRIM(ic), stage, cb, COALESCE(ca, ua)
FROM (
          SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('ERC_RM' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM rm_ic_edit
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('ERC_RM' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM rm_ic_save_changes
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('ERC_PROCESS' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM PROCESS_IC_EDIT
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('ERC_PROCESS' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM PROCESS_IC_SAVE_CHANGES
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('ERC_FINAL' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM FINAL_IC_EDIT
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('ERC_FINAL' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM FINAL_IC_SAVE_CHANGES
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('SLEEPER_FINAL' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM sleeper_final_ic_edit
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('SLEEPER_FINAL' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM sleeper_final_ic_save_changes
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('RAILPAD_PROCESS' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM railpad_process_ic_edit
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('RAILPAD_PROCESS' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM railpad_process_ic_save_changes
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('RAILPAD_FINAL' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM railpad_final_ic_edit
UNION ALL SELECT CONVERT(book_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS b, CONVERT(set_no USING utf8mb4) COLLATE utf8mb4_unicode_ci AS s, CONVERT(ic_number USING utf8mb4) COLLATE utf8mb4_unicode_ci AS ic, CONVERT(CAST(created_by AS CHAR) USING utf8mb4) COLLATE utf8mb4_unicode_ci AS cb, created_at AS ca, updated_at AS ua, CAST('RAILPAD_FINAL' AS CHAR(30)) COLLATE utf8mb4_unicode_ci AS stage FROM railpad_final_ic_save_changes
) src
WHERE b IS NOT NULL AND TRIM(b) <> ''
  AND s IS NOT NULL AND TRIM(s) <> ''
  AND ic IS NOT NULL AND TRIM(ic) <> '';

-- rows already written by the app take part in the "earliest wins" comparison
INSERT INTO tmp_bookset_all (book_no, set_no, ic_number, product_stage, user_id, used_at)
SELECT book_no, set_no, ic_number, product_stage, user_id, created_at FROM ic_book_set_usage;

-- 2. Earliest IC per Book/Set (MySQL can't read one temporary table twice in a query, so in two steps)
DROP TEMPORARY TABLE IF EXISTS tmp_bookset_firstat;
CREATE TEMPORARY TABLE tmp_bookset_firstat (KEY (book_no, set_no)) AS
SELECT book_no, set_no, MIN(COALESCE(used_at, '2000-01-01')) AS first_at
FROM tmp_bookset_all GROUP BY book_no, set_no;

DROP TEMPORARY TABLE IF EXISTS tmp_bookset_first;
CREATE TEMPORARY TABLE tmp_bookset_first AS
SELECT t.*
FROM tmp_bookset_all t
JOIN tmp_bookset_firstat f
  ON f.book_no = t.book_no AND f.set_no = t.set_no AND COALESCE(t.used_at, '2000-01-01') = f.first_at;

-- 3. Write it: the earliest IC owns each Book/Set (replaces a newer claim the app may have recorded)
INSERT INTO ic_book_set_usage (book_no, set_no, ic_key, ic_number, product_stage, user_id, created_at, updated_at)
SELECT book_no, set_no, MIN(ic_key), MIN(ic_number), MIN(product_stage), MIN(user_id),
       COALESCE(MIN(used_at), NOW()), NOW()
FROM tmp_bookset_first
GROUP BY book_no, set_no
ON DUPLICATE KEY UPDATE
       ic_key        = VALUES(ic_key),
       ic_number     = VALUES(ic_number),
       product_stage = VALUES(product_stage),
       user_id       = VALUES(user_id),
       created_at    = VALUES(created_at),
       updated_at    = NOW();

-- 4. Review: Book/Set already shared by more than one IC before this rule (first owner is kept above)
SELECT book_no, set_no,
       COUNT(DISTINCT ic_key) AS ic_count,
       GROUP_CONCAT(DISTINCT ic_number ORDER BY used_at SEPARATOR ' | ') AS ics_oldest_first
FROM tmp_bookset_all
GROUP BY book_no, set_no
HAVING COUNT(DISTINCT ic_key) > 1
ORDER BY ic_count DESC, book_no, set_no;

-- 5. Check
SELECT COUNT(*) AS book_sets_registered FROM ic_book_set_usage;
SELECT * FROM ic_book_set_usage WHERE book_no = '9999' AND set_no = '999';   -- expect W/ER-02180005/KJM

DROP TEMPORARY TABLE IF EXISTS tmp_bookset_first;
DROP TEMPORARY TABLE IF EXISTS tmp_bookset_firstat;
DROP TEMPORARY TABLE IF EXISTS tmp_bookset_all;
