-- =====================================================================
-- V20261111_006 : UDM Complaint — Phase 3a: SBU Head review
-- SBU Head: Forward to Railway Board  |  Return to CM for rework (RCA -> CAPA again, next round)
-- Run AFTER V20261111_005 (Railway Board role must exist). Safe to re-run.
-- Role ids are resolved by NAME (they differ between UAT and PROD).
-- =====================================================================

SET @UDM_WF   := (SELECT WORKFLOWID FROM WORKFLOW_MASTER WHERE WORKFLOWNAME = 'UDM_COMPLAINT' LIMIT 1);
SET @CM_ROLE  := (SELECT ROLEID FROM ROLE_MASTER
                  WHERE LOWER(TRIM(ROLENAME)) IN ('control manager', 'controlling manager', 'cm')
                  ORDER BY ROLEID LIMIT 1);
SET @SBU_ROLE := (SELECT ROLEID FROM ROLE_MASTER WHERE LOWER(TRIM(ROLENAME)) = 'sbu head' ORDER BY ROLEID LIMIT 1);
SET @RB_ROLE  := (SELECT ROLEID FROM ROLE_MASTER WHERE LOWER(TRIM(ROLENAME)) = 'railway board' ORDER BY ROLEID LIMIT 1);

-- Guard: stops here ("Subquery returns more than 1 row") if the workflow or any role is missing.
SET @guard := (SELECT 1 FROM (SELECT 1 AS x UNION ALL SELECT 2) t
               WHERE @UDM_WF IS NULL OR @CM_ROLE IS NULL OR @SBU_ROLE IS NULL OR @RB_ROLE IS NULL OR x = 1);

START TRANSACTION;

-- 1) TRANSITIONS
INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'SBU_FORWARD_TO_RB', @UDM_WF, @SBU_ROLE, @RB_ROLE, 10, 'Admin', NOW(), NULL, 'SBU_FORWARD_TO_RB', 'RB_REVIEW'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'SBU_FORWARD_TO_RB');

INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'SBU_RETURN_TO_CM', @UDM_WF, @SBU_ROLE, @CM_ROLE, 11, 'Admin', NOW(), NULL, 'SBU_RETURN_TO_CM', 'CM_SUBMIT_RCA'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'SBU_RETURN_TO_CM');

COMMIT;

-- 2) Rework round on the complaint (1 = first submission; +1 each time it is returned to the CM)
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'udm_complaint' AND COLUMN_NAME = 'current_round'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE udm_complaint ADD COLUMN current_round INT NOT NULL DEFAULT 1 AFTER last_remarks',
    'SELECT ''current_round already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- VERIFY (read only)
-- SELECT TRANSITIONNAME, CURRENTROLEID, NEXTROLEID FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF ORDER BY TRANSITIONORDER;
-- SHOW COLUMNS FROM udm_complaint LIKE 'current_round';
--
-- ASSIGN USERS to SBU Head (edit USERID):
-- INSERT INTO USER_ROLE_MASTER (USERID, ROLEID, CREATEDBY, CREATEDDATE) VALUES (<USERID>, @SBU_ROLE, 'Admin', NOW());
