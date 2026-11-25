-- =====================================================================
-- V20261111_001 : UDM Complaint Cell — role, workflow, transitions, tables
-- Phase 1: Complaint Cell can "Send to Controlling Manager" or "Close".
-- Run in MySQL Workbench (DB: the SARTHI schema). Safe to re-run (idempotent).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0) PRE-CHECK (read only) — confirm role names / ids before running
-- ---------------------------------------------------------------------
-- SELECT ROLEID, ROLENAME FROM ROLE_MASTER ORDER BY ROLEID;
-- SELECT WORKFLOWID, WORKFLOWNAME FROM WORKFLOW_MASTER ORDER BY WORKFLOWID;
-- Role ids are looked up by NAME below (UAT and PROD ids differ).

START TRANSACTION;

-- ---------------------------------------------------------------------
-- 1) ROLE: Complaint Cell
-- ---------------------------------------------------------------------
INSERT INTO ROLE_MASTER (ROLENAME, CREATEDBY, CREATEDDATE)
SELECT 'Complaint Cell', 'Admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ROLE_MASTER WHERE ROLENAME = 'Complaint Cell');

SET @CC_ROLE  := (SELECT ROLEID FROM ROLE_MASTER WHERE ROLENAME = 'Complaint Cell' LIMIT 1);
-- Role ids differ between UAT and PROD: always resolve by name, never hard-code.
SET @CM_ROLE  := (SELECT ROLEID FROM ROLE_MASTER
                  WHERE LOWER(TRIM(ROLENAME)) IN ('control manager', 'controlling manager', 'cm')
                  ORDER BY ROLEID LIMIT 1);
-- Guard: stops the script ("Subquery returns more than 1 row") if the CM role is not found.
SET @guard := (SELECT 1 FROM (SELECT 1 AS x UNION ALL SELECT 2) t WHERE @CM_ROLE IS NULL OR x = 1);

-- ---------------------------------------------------------------------
-- 2) WORKFLOW: UDM_COMPLAINT
-- ---------------------------------------------------------------------
INSERT INTO WORKFLOW_MASTER (WORKFLOWNAME, CREATEDBY, CREATEDDATE)
SELECT 'UDM_COMPLAINT', 'Admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM WORKFLOW_MASTER WHERE WORKFLOWNAME = 'UDM_COMPLAINT');

SET @UDM_WF := (SELECT WORKFLOWID FROM WORKFLOW_MASTER WHERE WORKFLOWNAME = 'UDM_COMPLAINT' LIMIT 1);

-- ---------------------------------------------------------------------
-- 3) TRANSITIONS (Phase 1)
--    TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER,
--    CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION
-- ---------------------------------------------------------------------
INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'UDM_COMPLAINT_RECEIVED', @UDM_WF, @CC_ROLE, @CC_ROLE, 1, 'Admin', NOW(), NULL, 'UDM_COMPLAINT_RECEIVED', 'CC_DECISION'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'UDM_COMPLAINT_RECEIVED');

INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'CC_SEND_TO_CM', @UDM_WF, @CC_ROLE, @CM_ROLE, 2, 'Admin', NOW(), NULL, 'CC_SEND_TO_CM', 'CM_DECISION'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'CC_SEND_TO_CM');

INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'CC_CLOSE_COMPLAINT', @UDM_WF, @CC_ROLE, @CC_ROLE, 3, 'Admin', NOW(), NULL, 'CC_CLOSE_COMPLAINT', 'CLOSED'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'CC_CLOSE_COMPLAINT');

-- ---------------------------------------------------------------------
-- 4) TABLE: udm_complaint  (one row per UDM rejection = current state)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS udm_complaint (
    id                       BIGINT        NOT NULL AUTO_INCREMENT,
    udm_rejection_id         BIGINT        NOT NULL,
    rejection_number         VARCHAR(100)  NOT NULL,
    status                   VARCHAR(40)   NOT NULL DEFAULT 'PENDING_WITH_COMPLAINT_CELL',
                                           -- PENDING_WITH_COMPLAINT_CELL | SENT_TO_CM | CLOSED
    pending_with_role_id     INT           NULL,
    pending_with_role_name   VARCHAR(100)  NULL,
    assigned_rio             VARCHAR(50)   NULL,
    assigned_cm_user_id      INT           NULL,
    assigned_cm_name         VARCHAR(150)  NULL,
    last_remarks             VARCHAR(500)  NULL,
    closed_by                INT           NULL,
    closed_at                DATETIME      NULL,
    created_by               INT           NULL,
    created_at               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by               INT           NULL,
    updated_at               DATETIME      NULL ON UPDATE CURRENT_TIMESTAMP,
    version                  INT           NOT NULL DEFAULT 0,   -- optimistic lock (no double action)
    PRIMARY KEY (id),
    UNIQUE KEY uk_udm_complaint_rejection (udm_rejection_id),
    KEY idx_udm_complaint_status (status),
    KEY idx_udm_complaint_cm (assigned_cm_user_id, status),
    KEY idx_udm_complaint_rejno (rejection_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 5) TABLE: udm_complaint_workflow_transition  (audit trail / timeline)
--    Kept separate from WORKFLOW_TRANSITION so UDM rows never appear in
--    CM / IE inspection-call pending lists that read WORKFLOW_TRANSITION.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS udm_complaint_workflow_transition (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    complaint_id         BIGINT        NOT NULL,
    udm_rejection_id     BIGINT        NOT NULL,
    rejection_number     VARCHAR(100)  NOT NULL,
    workflow_id          INT           NOT NULL,
    transition_id        INT           NOT NULL,
    action               VARCHAR(60)   NOT NULL,
    current_role_id      INT           NULL,
    current_role_name    VARCHAR(100)  NULL,
    next_role_id         INT           NULL,
    next_role_name       VARCHAR(100)  NULL,
    current_status       VARCHAR(40)   NULL,
    next_status          VARCHAR(40)   NULL,
    remarks              VARCHAR(500)  NULL,
    assigned_rio         VARCHAR(50)   NULL,
    assigned_to_user     INT           NULL,
    created_by           INT           NULL,
    created_date         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_udm_cwt_complaint (complaint_id, created_date),
    KEY idx_udm_cwt_pending (next_role_id, assigned_to_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;

-- ---------------------------------------------------------------------
-- 6) VERIFY (read only)
-- ---------------------------------------------------------------------
-- SELECT ROLEID, ROLENAME FROM ROLE_MASTER WHERE ROLENAME = 'Complaint Cell';
-- SELECT * FROM WORKFLOW_MASTER WHERE WORKFLOWNAME = 'UDM_COMPLAINT';
-- SELECT * FROM TRANSITION_MASTER WHERE WORKFLOWID = (SELECT WORKFLOWID FROM WORKFLOW_MASTER WHERE WORKFLOWNAME='UDM_COMPLAINT');
-- SHOW CREATE TABLE udm_complaint;
-- SHOW CREATE TABLE udm_complaint_workflow_transition;

-- ---------------------------------------------------------------------
-- 7) ASSIGN USERS to Complaint Cell (edit USERID, then run)
-- ---------------------------------------------------------------------
-- INSERT INTO USER_ROLE_MASTER (USERID, ROLEID, CREATEDBY, CREATEDDATE)
-- VALUES (<USERID>, @CC_ROLE, 'Admin', NOW());
