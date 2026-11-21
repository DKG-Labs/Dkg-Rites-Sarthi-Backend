-- Issue 13: Sleeper Final Inspection (IC Issuance) for RMT UOM.
-- Offered / Accepted / Rejected quantities can be fractional (e.g. 4524.36 RMT), so the
-- final_call_inspection_header qty columns move from INT to DECIMAL(12,2), matching
-- sleeper_inspection_call.total_offered. Existing whole-number values convert without loss.
ALTER TABLE final_call_inspection_header
    MODIFY qty_offered_now DECIMAL(12,2) NULL,
    MODIFY accepted_qty    DECIMAL(12,2) NULL,
    MODIFY rejected_qty    DECIMAL(12,2) NULL;
