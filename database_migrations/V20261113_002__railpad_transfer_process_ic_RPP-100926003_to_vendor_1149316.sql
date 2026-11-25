-- Rail Pad: make Process IC RPP-100926003 (vendor 49923) usable for a Final call under new vendor 1149316
-- PO 06260116102581 (SECR, amended under new vendor ID).
--
-- WHY IT IS NOT VISIBLE: the final-call form (GET /process-calls) does NOT filter by vendor.
-- It matches rail_inspection_call on: call_type PROCESS, status SEND_CALL_TO_IBS, rail_pad_type,
-- drawing_no, plant_id (POI) and po_no + po_sr. After the vendor change the new vendor's plant (POI)
-- and/or the amended PO serial differ, so the old process call no longer matches.
--
-- STEP 1 = read-only. Run it, compare old call vs new PO, then fill STEP 2 values. Take a backup first.

-- ===== STEP 1: READ-ONLY CHECKS =====
-- 1a. The old process call
SELECT id, call_no, po_no, po_sr, vendor_code, plant_id, rail_pad_type, drawing_no, status, call_type, total_qty, process_ic_no
FROM rail_inspection_call
WHERE call_no = 'RPP-100926003';

-- 1b. Its completion / certificate row (stores PO as "PO" or "PO/SR")
SELECT * FROM RAIL_INSPECTION_COMPLETE_DETAILS WHERE CALL_NO = 'RPP-100926003';

-- 1c. Any calls already raised on this PO (both vendors) - to see the new vendor's plant_id / po_sr
SELECT call_no, po_no, po_sr, vendor_code, plant_id, rail_pad_type, drawing_no, status, call_type
FROM rail_inspection_call
WHERE po_no LIKE '06260116102581%'
ORDER BY created_at;

-- 1d. Plants (POI) of old and new vendor
SELECT * FROM rail_vendor_plant WHERE vendor_code IN ('49923', '1149316');
SELECT * FROM railpad_pincode_poi_mapping WHERE vendor_code IN ('49923', '1149316');

-- ===== STEP 2: TRANSFER (run only after checking STEP 1; replace <...>) =====
-- Keep the IC, accepted qty and history; only re-point ownership/plant/serial to the new vendor's PO.
START TRANSACTION;
--
UPDATE rail_inspection_call
   SET vendor_code = '1149316',
       plant_id    = '1149316/Faridabad',
       remarks     = CONCAT(COALESCE(remarks, ''), ' | Transferred from vendor 49923 to 1149316 on vendor request, ', DATE_FORMAT(NOW(), '%d-%m-%Y')),
       updated_at  = NOW()
 WHERE call_no = 'RPP-100926003'
   AND vendor_code = '49923'
   AND plant_id = '49923/Faridabad';
--
-- -- only if PO_NO in 1b holds an old serial, e.g. '06260116102581/<OLD_SR>':
-- -- UPDATE RAIL_INSPECTION_COMPLETE_DETAILS SET PO_NO = '06260116102581/<NEW_PO_SR>' WHERE CALL_NO = 'RPP-100926003';
--
-- -- verify: should return the call for the new vendor's plant
SELECT call_no, vendor_code, plant_id, po_no, po_sr, status FROM rail_inspection_call WHERE call_no = 'RPP-100926003';
--
-- Expect 1 row changed. Then:
-- COMMIT;   -- or ROLLBACK;
