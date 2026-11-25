-- Sleeper calls auto-forwarded to the Main IE got status 'RIO_VERIFIED' on their VERIFY row
-- (SleeperWorkflowServiceImpl.initiateWorkflow, since commit 65508aee on 17-Nov-2026), so the Main IE
-- "List of Calls Pending" (status IN CREATED/PENDING) did not show them. Code now writes 'PENDING'.
-- This fixes the rows already saved. job_status stays 'RIO_VERIFIED'.

-- 1. Check (read-only): latest row per call that is stuck
SELECT t.workflow_transition_id, t.request_id, t.status, t.job_status, t.assigned_to_user, t.created_date
FROM sleeper_workflow_transaction t
JOIN (SELECT request_id, MAX(workflow_transition_id) mx FROM sleeper_workflow_transaction GROUP BY request_id) m
  ON m.mx = t.workflow_transition_id
WHERE UPPER(t.status) = 'RIO_VERIFIED' AND t.action = 'VERIFY';
-- On 18-Nov-2026 prod: SF-26111809 (56800), SF-26111811 (56864), SF-26111810 (56916), SF-26111812 (56919)

-- 2. Fix (safe-update mode: uses the key column)
START TRANSACTION;
UPDATE sleeper_workflow_transaction
   SET status = 'PENDING'
 WHERE workflow_transition_id IN (56800, 56864, 56916, 56919)
   AND status = 'RIO_VERIFIED' AND action = 'VERIFY';
-- Expect 4 rows changed (fewer if a call has moved on). Then:
-- COMMIT;   -- or ROLLBACK;
