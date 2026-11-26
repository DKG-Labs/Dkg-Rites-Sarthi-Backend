-- Report performance indexes (Monthly Analysis of Units, Quality of PSC Sleepers, Sleeper summary).
-- Index-only change: no data is modified. Each statement is independent; if one fails with
-- "Duplicate key name", that index already exists and the statement can be skipped.

-- 1. Monthly Analysis of Units: process rejections filter demoulding inspections by created_date.
--    (Existing: idx_di_batch_no, idx_di_batch_date (batch_no, inspection_date), idx_ddi_plant (plant_id))
CREATE INDEX idx_di_created_plant ON demoulding_inspection (created_date, plant_id);

-- 2. Monthly Analysis of Units: production filters declarations by created_date.
--    (Existing: idx_pd_casting_date, idx_pd_plant_casting_date (plant_id, casting_date))
CREATE INDEX idx_pd_created_plant ON production_declaration (created_date, plant_id);

-- 3. Quality of PSC Sleepers "categories" / "produced": bench groups and gangs looked up by declaration.
--    Check first with SHOW INDEX FROM <table>; skip any table that already has an index starting with the column.
CREATE INDEX idx_psc_declaration ON production_stress_chamber (declaration_id);
CREATE INDEX idx_pbg_chamber ON production_bench_group (chamber_id);
CREATE INDEX idx_plg_declaration ON production_longline_gang (declaration_id);

-- 4. Latest workflow transaction per request (used by "produced" and the IC queries).
CREATE INDEX idx_swt_module_request ON sleeper_workflow_transaction (module_id, request_id, workflow_transition_id);
CREATE INDEX idx_swt_workflow_request ON sleeper_workflow_transaction (workflow_id, request_id, workflow_transition_id);

-- Check afterwards (each should list the new index):
-- SHOW INDEX FROM demoulding_inspection WHERE Key_name = 'idx_di_created_plant';
-- SHOW INDEX FROM production_declaration WHERE Key_name = 'idx_pd_created_plant';
