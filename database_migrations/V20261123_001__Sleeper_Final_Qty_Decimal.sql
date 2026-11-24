-- Sleeper Final Inspection for measured UOMs (e.g. Mtr / RMT): PO Sr. qty and call qty can be fractional
-- (e.g. 4238.25 Mtr), so these columns move from INT to DECIMAL(12,2), matching
-- final_call_inspection_header qty_offered_now / accepted_qty / rejected_qty (V20261121_001).
-- Existing whole-number values convert without loss.
--
-- Before running, check sleeper_final_result:
--   SHOW COLUMNS FROM sleeper_final_result WHERE Field IN ('total_offered_quantity','total_accepted','total_rejected');
-- If any of them is INT, also run: ALTER TABLE sleeper_final_result MODIFY <column> DECIMAL(12,2) NULL;

ALTER TABLE final_call_inspection_header
    MODIFY po_qty DECIMAL(12,2) NULL;

ALTER TABLE final_call_inspection_section_a
    MODIFY po_qty DECIMAL(12,2) NULL;

ALTER TABLE final_call_inspection_section_b
    MODIFY call_qty_mt DECIMAL(12,2) NULL;
