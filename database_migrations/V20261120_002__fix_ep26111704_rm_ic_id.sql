-- EP-26111704 (process ic_id 1594) was offered from RM IC N/ER-26110306/MKL (id 1392):
-- process_rm_ic_mapping has 1392 and ER-26110306 accepted exactly 15798 for heat J854502 = offered 15798.
-- But process_inspection_details row 1135 has rm_ic_id = 1555 (= ER-26111315), so its 15798 was counted as
-- "Offered Earlier" under ER-26111315 and reduced that RM IC's balance.

-- 1. Check (read-only)
SELECT id, ic_id, rm_ic_id, rm_ic_number, heat_number, offered_qty FROM process_inspection_details WHERE id = 1135;
-- expect rm_ic_id 1555, rm_ic_number N/ER-26110306/MKL
SELECT id, ic_number FROM inspection_calls WHERE id IN (1392, 1555);   -- 1392 ER-26110306, 1555 ER-26111315

-- Any other rows where rm_ic_id does not match rm_ic_number (review before fixing)
SELECT p.id, p.ic_id, p.rm_ic_id, c.ic_number AS id_points_to, p.rm_ic_number, p.heat_number, p.offered_qty
  FROM process_inspection_details p
  JOIN inspection_calls c ON c.id = p.rm_ic_id
 WHERE p.rm_ic_number IS NOT NULL AND p.rm_ic_number <> ''
   AND p.rm_ic_number NOT LIKE CONCAT('%', c.ic_number COLLATE utf8mb4_unicode_ci, '%');
-- (checked 20/11/2026: only row 1135)

START TRANSACTION;

-- 2. Fix
UPDATE process_inspection_details SET rm_ic_id = 1392
 WHERE id = 1135 AND rm_ic_id = 1555 AND rm_ic_number = 'N/ER-26110306/MKL';
-- expect 1 row

-- 3. Verify
SELECT id, rm_ic_id, rm_ic_number FROM process_inspection_details WHERE id = 1135;   -- 1392

-- COMMIT;   -- or ROLLBACK;
