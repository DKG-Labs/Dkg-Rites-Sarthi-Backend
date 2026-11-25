-- ERC calls auto-registered to the mapped IE (action AUTO_REGISTER, commit 65508aee 17-Nov-2026) were hidden
-- from the IE / Process IE pending lists:
--   * job_status was 'CALL_REGISTERED'; the IE list queries only accept 'VERIFIED' (what the manual verify saves).
--   * Process (EP) calls also got NEXTROLE 3 'IE' instead of 7 'Process IE', and were assigned to one IE,
--     while the manual verify leaves Process calls unassigned (shown to every Process IE mapped to the POI).
-- Code is fixed; this corrects the rows already saved.

-- 1. Check (read-only): latest row of auto-registered calls
SELECT t.WORKFLOWTRANSITIONID, t.REQUESTID, t.job_status, t.NEXTROLE, t.next_role_Name, t.assigned_to_user, t.process_ie_user_id, t.CREATEDDATE
FROM workflow_transition t
JOIN (SELECT REQUESTID, MAX(WORKFLOWTRANSITIONID) mx FROM workflow_transition GROUP BY REQUESTID) m
  ON m.mx = t.WORKFLOWTRANSITIONID
WHERE t.ACTION = 'AUTO_REGISTER';
-- On 18-Nov-2026 prod: EF-26111801 (37851), EF-26111802 (37854), EF-26111803 (37868), EP-26111806 (37871)

START TRANSACTION;

-- 2a. All auto-registered calls: job status as the manual verify
UPDATE workflow_transition
   SET job_status = 'VERIFIED'
 WHERE WORKFLOWTRANSITIONID IN (37851, 37854, 37868, 37871)
   AND ACTION = 'AUTO_REGISTER' AND job_status = 'CALL_REGISTERED';
-- Expect 4 rows

-- 2b. Process call EP-26111806: Process IE role, no single-IE assignment (like the manual verify)
UPDATE workflow_transition
   SET NEXTROLE = '7', next_role_Name = 'Process IE', assigned_to_user = NULL, process_ie_user_id = NULL
 WHERE WORKFLOWTRANSITIONID IN (37871)
   AND ACTION = 'AUTO_REGISTER' AND NEXTROLE = '3';
-- Expect 1 row

-- Check, then:
-- COMMIT;   -- or ROLLBACK;
