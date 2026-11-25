-- Rail Pad calls RPP-091826005 / 006 / 007 (POI14, 13062/Bankura) were assigned to user 1077
-- (Devendra Kumar, emp 104713, mapped only to POI08). The IE mapped to POI14 with the same name is
-- user 1273 (DEVENDRA KUMAR, emp 105241), so the calls never showed on his dashboard.

-- 1. Check (read-only)
SELECT workflow_transition_id, request_id, next_role, assigned_to_user, status
  FROM rail_workflow_transaction
 WHERE request_id IN ('RPP-091826005', 'RPP-091826006', 'RPP-091826007')
 ORDER BY request_id, workflow_transition_id;

START TRANSACTION;

-- 2. Reassign the open rows of these 3 calls from 1077 to 1273
UPDATE rail_workflow_transaction
   SET assigned_to_user = 1273
 WHERE request_id IN ('RPP-091826005', 'RPP-091826006', 'RPP-091826007')
   AND assigned_to_user = 1077
   AND poi_code = 'POI14';
-- Expect 6 rows (2 per call: CREATED and VERIFY), matching step 1.

-- 3. Verify
SELECT workflow_transition_id, request_id, assigned_to_user
  FROM rail_workflow_transaction
 WHERE request_id IN ('RPP-091826005', 'RPP-091826006', 'RPP-091826007');
-- Then:
-- COMMIT;   -- or ROLLBACK;
