-- Indexes for the slow sleeper queries. Run on sarthidb. If an index already exists MySQL will error
-- on that line; check with SHOW INDEX FROM <table> first and skip the ones already there.
-- On large tables use ALGORITHM=INPLACE, LOCK=NONE (InnoDB) to avoid blocking writes.

ALTER TABLE production_sleeper         ADD INDEX idx_ps_bench_group (bench_group_id), ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE production_sleeper         ADD INDEX idx_ps_gang        (gang_id),        ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE production_bench_group     ADD INDEX idx_pbg_chamber    (chamber_id),     ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE production_stress_chamber  ADD INDEX idx_psc_decl       (declaration_id), ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE production_longline_gang   ADD INDEX idx_plg_decl       (declaration_id), ALGORITHM=INPLACE, LOCK=NONE;

-- Sleeper inspection-call list (the 1284s query)
ALTER TABLE sleeper_workflow_transaction ADD INDEX idx_swt_wf_req (workflow_id, request_id, workflow_transition_id), ALGORITHM=INPLACE, LOCK=NONE;
-- sleeper_inspection_call(call_no) already has a UNIQUE index named call_no - no new index needed.

-- Joined tables used by the same query (skip any that already exist)
ALTER TABLE vendor_plant ADD INDEX idx_vp_plant_id (plant_id), ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE po_header    ADD INDEX idx_ph_po_no (po_no),       ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE po_item      ADD INDEX idx_pi_hdr_sr (po_header_id, item_sr_no), ALGORITHM=INPLACE, LOCK=NONE;

-- Dashboard "final accepted" / "ic issued" queries
ALTER TABLE sleeper_inspection_complete_details ADD INDEX idx_sicd_call_no (call_no), ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE sleeper_final_result  ADD INDEX idx_sfr_call_number (call_number), ALGORITHM=INPLACE, LOCK=NONE;
ALTER TABLE sleeper_final_ic_edit ADD INDEX idx_sfie_ic_number (ic_number),   ALGORITHM=INPLACE, LOCK=NONE;

-- Verify afterwards:  EXPLAIN <query>;  the key column should no longer be NULL.
