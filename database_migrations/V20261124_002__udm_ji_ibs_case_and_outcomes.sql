-- =====================================================================
-- V20261124_002 : UDM Rejection – JI Declaration by CM / Complete JI
--   * JI Declaration: new fields IBS Case Number + Date of 1st Reference (on the DECISION stage row)
--   * Complete JI: new JI Outcome options (old ones kept but inactive, so history still shows)
-- Safe to re-run.
-- =====================================================================

SET @db := DATABASE();

SET @s := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='udm_complaint_stage' AND COLUMN_NAME='ibs_case_no')=0,
  'ALTER TABLE udm_complaint_stage ADD COLUMN ibs_case_no VARCHAR(50) NULL AFTER ji_team_user_ids', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='udm_complaint_stage' AND COLUMN_NAME='first_reference_date')=0,
  'ALTER TABLE udm_complaint_stage ADD COLUMN first_reference_date DATE NULL AFTER ibs_case_no', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

START TRANSACTION;

-- retire the old JI outcomes (rows kept for complaints that already used them)
UPDATE udm_complaint_dropdown_master SET is_active = 0
WHERE category = 'JI_OUTCOME'
  AND value NOT IN ('Upheld','Accepted','Not on RITES Account','Transit Damage','Deleted','Lifted before JI');

-- new JI outcomes, in this order
INSERT INTO udm_complaint_dropdown_master (category, value, sort_order, is_active)
SELECT * FROM (
  SELECT 'JI_OUTCOME' c, 'Upheld' v, 1 o, 1 a UNION ALL
  SELECT 'JI_OUTCOME', 'Accepted', 2, 1 UNION ALL
  SELECT 'JI_OUTCOME', 'Not on RITES Account', 3, 1 UNION ALL
  SELECT 'JI_OUTCOME', 'Transit Damage', 4, 1 UNION ALL
  SELECT 'JI_OUTCOME', 'Deleted', 5, 1 UNION ALL
  SELECT 'JI_OUTCOME', 'Lifted before JI', 6, 1
) n
WHERE NOT EXISTS (SELECT 1 FROM udm_complaint_dropdown_master d WHERE d.category = n.c AND d.value = n.v);

-- make sure the six are active with the right order (if they existed before)
UPDATE udm_complaint_dropdown_master SET is_active = 1,
  sort_order = FIELD(value,'Upheld','Accepted','Not on RITES Account','Transit Damage','Deleted','Lifted before JI')
WHERE category = 'JI_OUTCOME'
  AND value IN ('Upheld','Accepted','Not on RITES Account','Transit Damage','Deleted','Lifted before JI');

COMMIT;

-- VERIFY (read only)
SELECT value, sort_order, is_active FROM udm_complaint_dropdown_master WHERE category = 'JI_OUTCOME' ORDER BY is_active DESC, sort_order;
