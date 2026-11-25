-- ERC diversion ICs ER-26111801 (RM, partial) and EF-26102901 (Final, complete).
--
-- IBS takes the IC number from the IC edit table saved at e-sign (rm_ic_edit / final_ic_edit .ic_number),
-- and only when it is in "RIO/call/IE" form. Process diversion ICs saved "N/EP-.../IE" (the Process screen
-- builds that number); the RM and Final screens saved the bare call no, so IBS got icNumber = null.
-- The RM / Final screens now build the number like Process. This corrects the two ICs already issued, and
-- saves the diverted heats of ER-26111801 on the call itself (the code now does this on CM approval).
-- INSPECTION_COMPLETE_DETAILS is NOT needed for diversion ICs.

-- 1. Check (read-only)
SELECT 'rm_ic_edit' t, id, ic_number FROM rm_ic_edit WHERE ic_number = 'ER-26111801'                -- id 429
UNION ALL SELECT 'rm_ic_save_changes', id, ic_number FROM rm_ic_save_changes WHERE ic_number = 'ER-26111801'   -- id 433
UNION ALL SELECT 'final_ic_edit', id, ic_number FROM final_ic_edit WHERE ic_number = 'EF-26102901'              -- id 270
UNION ALL SELECT 'final_ic_save_changes', id, ic_number FROM final_ic_save_changes WHERE ic_number = 'EF-26102901'; -- id 242
SELECT * FROM rm_inspection_details WHERE ic_id = 1605;                                              -- expect none

START TRANSACTION;

-- 2a. IC number in "RIO/call/IE" form (NRIO; IE short names as stored: 168 RTS, 213 Harvi), as the IC screen now shows
UPDATE rm_ic_edit            SET ic_number = 'N/ER-26111801/RTS'   WHERE id = 429 AND ic_number = 'ER-26111801';
UPDATE rm_ic_save_changes    SET ic_number = 'N/ER-26111801/RTS'   WHERE id = 433 AND ic_number = 'ER-26111801';
UPDATE final_ic_edit         SET ic_number = 'N/EF-26102901/Harvi' WHERE id = 270 AND ic_number = 'EF-26102901';
UPDATE final_ic_save_changes SET ic_number = 'N/EF-26102901/Harvi' WHERE id = 242 AND ic_number = 'EF-26102901';
-- Expect 1 row each

-- 2b. ER-26111801: RM details of the diversion call (id 1605) = copy of the source call's (id 1257), only the diverted heats
INSERT INTO rm_inspection_details (ic_id, item_description, item_quantity, consignee_zonal_railway, heat_numbers,
  tc_number, tc_date, tc_quantity, manufacturer, supplier_name, supplier_address, invoice_number, invoice_date,
  sub_po_number, sub_po_date, sub_po_qty, total_offered_qty_mt, offered_qty_erc, unit_of_measurement,
  rate_of_material, rate_of_gst, base_value_po, total_po, created_at, updated_at)
SELECT 1605, s.item_description, s.item_quantity, s.consignee_zonal_railway, '26D03216, 26D03220',
  s.tc_number, s.tc_date, s.tc_quantity, s.manufacturer, s.supplier_name, s.supplier_address, s.invoice_number, s.invoice_date,
  s.sub_po_number, s.sub_po_date, s.sub_po_qty, 57.222, s.offered_qty_erc, s.unit_of_measurement,
  s.rate_of_material, s.rate_of_gst, s.base_value_po, s.total_po, NOW(), NOW()
  FROM rm_inspection_details s
 WHERE s.ic_id = 1257
   AND NOT EXISTS (SELECT 1 FROM rm_inspection_details x WHERE x.ic_id = 1605);
-- Expect 1 row

-- 2c. The 2 diverted heats at their diverted qty (26D03216 18.562, 26D03220 38.660 = 57.222 MT)
INSERT INTO rm_heat_quantities (rm_detail_id, heat_number, manufacturer, offered_qty, tc_number, tc_date, tc_quantity,
  qty_left, qty_accepted, qty_rejected, rejection_reason, color_code, created_at, updated_at)
SELECT (SELECT id FROM rm_inspection_details WHERE ic_id = 1605), h.heat_number, h.manufacturer, d.qty, h.tc_number, h.tc_date, h.tc_quantity,
       0, d.qty, 0, NULL, h.color_code, NOW(), NOW()
  FROM rm_heat_quantities h
  JOIN (SELECT MIN(hq.id) id FROM rm_heat_quantities hq
          JOIN rm_inspection_details rd ON rd.id = hq.rm_detail_id
         WHERE rd.ic_id = 1257 AND UPPER(TRIM(hq.heat_number)) IN ('26D03216', '26D03220')
         GROUP BY UPPER(TRIM(hq.heat_number))) one ON one.id = h.id
  JOIN (SELECT UPPER(TRIM(i.heat_no)) heat, SUM(i.diversion_qty) qty
          FROM erc_material_diversion_item i JOIN erc_material_diversion_request r ON r.id = i.diversion_request_id
         WHERE r.request_no = 'ER-26111801' GROUP BY UPPER(TRIM(i.heat_no))) d ON d.heat = UPPER(TRIM(h.heat_number))
 WHERE NOT EXISTS (SELECT 1 FROM rm_heat_quantities x JOIN rm_inspection_details rx ON rx.id = x.rm_detail_id WHERE rx.ic_id = 1605);
-- Expect 2 rows

-- 3. Verify
SELECT ic_number FROM rm_ic_edit WHERE id = 429;            -- N/ER-26111801/RTS
SELECT ic_number FROM final_ic_edit WHERE id = 270;         -- N/EF-26102901/Harvi
SELECT hq.heat_number, hq.offered_qty, hq.qty_accepted FROM rm_heat_quantities hq
  JOIN rm_inspection_details rd ON rd.id = hq.rm_detail_id WHERE rd.ic_id = 1605;   -- 2 rows
-- Then:
-- COMMIT;   -- or ROLLBACK;
