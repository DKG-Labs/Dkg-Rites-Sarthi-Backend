-- =====================================================================
-- V20261111_003 : UDM Complaint — Phase 2 (Controlling Manager stages)
-- CM Decision (JI / Audit) -> Complete JI / Complete Audit -> RCA -> CAPA (-> SBU Head)
-- Adds transitions + stage, document and dropdown tables.
-- Run AFTER V20261111_002 and V20261111_001. Safe to re-run (idempotent).
-- =====================================================================

-- Role ids are looked up by NAME below (UAT and PROD ids differ).
-- PRE-CHECK (read only): SELECT ROLEID, ROLENAME FROM ROLE_MASTER WHERE ROLENAME IN ('SBU Head','Control Manager','Controlling Manager','CM');

START TRANSACTION;

SET @UDM_WF  := (SELECT WORKFLOWID FROM WORKFLOW_MASTER WHERE WORKFLOWNAME = 'UDM_COMPLAINT' LIMIT 1);
-- Role ids differ between UAT and PROD: always resolve by name, never hard-code.
SET @CM_ROLE  := (SELECT ROLEID FROM ROLE_MASTER
                  WHERE LOWER(TRIM(ROLENAME)) IN ('control manager', 'controlling manager', 'cm')
                  ORDER BY ROLEID LIMIT 1);
SET @SBU_ROLE := (SELECT ROLEID FROM ROLE_MASTER WHERE LOWER(TRIM(ROLENAME)) = 'sbu head' ORDER BY ROLEID LIMIT 1);
-- Guard: stops the script ("Subquery returns more than 1 row") if the workflow or a role is not found.
SET @guard := (SELECT 1 FROM (SELECT 1 AS x UNION ALL SELECT 2) t
               WHERE @UDM_WF IS NULL OR @CM_ROLE IS NULL OR @SBU_ROLE IS NULL OR x = 1);

-- ---------------------------------------------------------------------
-- 1) TRANSITIONS (Phase 2)
-- ---------------------------------------------------------------------
INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'CM_SCHEDULE_JI', @UDM_WF, @CM_ROLE, @CM_ROLE, 4, 'Admin', NOW(), NULL, 'CM_SCHEDULE_JI', 'CM_COMPLETE_JI'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'CM_SCHEDULE_JI');

INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'CM_START_AUDIT', @UDM_WF, @CM_ROLE, @CM_ROLE, 5, 'Admin', NOW(), NULL, 'CM_START_AUDIT', 'CM_COMPLETE_AUDIT'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'CM_START_AUDIT');

INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'CM_COMPLETE_JI', @UDM_WF, @CM_ROLE, @CM_ROLE, 6, 'Admin', NOW(), NULL, 'CM_COMPLETE_JI', 'CM_SUBMIT_RCA'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'CM_COMPLETE_JI');

INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'CM_COMPLETE_AUDIT', @UDM_WF, @CM_ROLE, @CM_ROLE, 7, 'Admin', NOW(), NULL, 'CM_COMPLETE_AUDIT', 'CM_SUBMIT_RCA'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'CM_COMPLETE_AUDIT');

INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'CM_SUBMIT_RCA', @UDM_WF, @CM_ROLE, @CM_ROLE, 8, 'Admin', NOW(), NULL, 'CM_SUBMIT_RCA', 'CM_SUBMIT_CAPA'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'CM_SUBMIT_RCA');

INSERT INTO TRANSITION_MASTER
    (TRANSITIONNAME, WORKFLOWID, CURRENTROLEID, NEXTROLEID, TRANSITIONORDER, CREATEDBY, CREATEDDATE, condition_id, CURRENT_ACTION, NEXT_ACTION)
SELECT 'CM_SUBMIT_CAPA', @UDM_WF, @CM_ROLE, @SBU_ROLE, 9, 'Admin', NOW(), NULL, 'CM_SUBMIT_CAPA', 'SBU_REVIEW'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF AND TRANSITIONNAME = 'CM_SUBMIT_CAPA');

-- ---------------------------------------------------------------------
-- 2) TABLE: udm_complaint_stage  (one row per stage submission; round_no for rework)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS udm_complaint_stage (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    complaint_id         BIGINT        NOT NULL,
    udm_rejection_id     BIGINT        NOT NULL,
    stage                VARCHAR(30)   NOT NULL,   -- DECISION | JI_COMPLETE | AUDIT_COMPLETE | RCA | CAPA
    round_no             INT           NOT NULL DEFAULT 1,
    path                 VARCHAR(10)   NULL,       -- JI | AUDIT (DECISION stage)
    flag_failed_90       TINYINT(1)    NULL,       -- recommendation flags seen/confirmed by CM
    flag_qty_over_5      TINYINT(1)    NULL,
    flag_critical        TINYINT(1)    NULL,
    stage_date           DATE          NULL,       -- JI date (scheduled/actual) or audit date
    ji_team              VARCHAR(200)  NULL,
    ji_outcome           VARCHAR(60)   NULL,
    audit_type           VARCHAR(40)   NULL,       -- Audit of Record | Audit of Firm | Both
    findings             VARCHAR(1000) NULL,
    attributable_to      VARCHAR(60)   NULL,
    root_cause           VARCHAR(60)   NULL,
    corrective_actions   VARCHAR(600)  NULL,       -- '|' separated values from dropdown master
    preventive_actions   VARCHAR(600)  NULL,
    vendor_compliance    CHAR(1)       NULL,       -- Y | N
    capa_implemented     CHAR(1)       NULL,       -- Y | N
    remarks              VARCHAR(500)  NULL,
    created_by           INT           NULL,
    created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_udm_stage_complaint (complaint_id, stage, round_no),
    KEY idx_udm_stage_rejection (udm_rejection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3) TABLE: udm_complaint_document  (PDFs in Azure container sarthi-udm-complaint-docs)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS udm_complaint_document (
    id                   BIGINT        NOT NULL AUTO_INCREMENT,
    complaint_id         BIGINT        NOT NULL,
    udm_rejection_id     BIGINT        NOT NULL,
    stage                VARCHAR(30)   NOT NULL,   -- JI_COMPLETE | AUDIT_COMPLETE | RCA | CAPA
    round_no             INT           NOT NULL DEFAULT 1,
    doc_type             VARCHAR(40)   NOT NULL,   -- JI_REPORT | AUDIT_REPORT | RCA_EVIDENCE | CAPA_EVIDENCE
    original_name        VARCHAR(255)  NOT NULL,
    blob_name            VARCHAR(500)  NOT NULL,
    content_type         VARCHAR(100)  NOT NULL,
    size_bytes           BIGINT        NOT NULL,
    is_active            TINYINT(1)    NOT NULL DEFAULT 1,
    uploaded_by          INT           NULL,
    uploaded_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_udm_doc_complaint (udm_rejection_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 4) TABLE: udm_complaint_dropdown_master  (SRS dropdown values)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS udm_complaint_dropdown_master (
    id            INT           NOT NULL AUTO_INCREMENT,
    category      VARCHAR(40)   NOT NULL,   -- JI_OUTCOME | ATTRIBUTABLE_TO | ROOT_CAUSE | CORRECTIVE_ACTION | PREVENTIVE_ACTION | AUDIT_TYPE
    value         VARCHAR(60)   NOT NULL,
    sort_order    INT           NOT NULL DEFAULT 0,
    is_active     TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_udm_dd (category, value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT IGNORE INTO udm_complaint_dropdown_master (category, value, sort_order) VALUES
('JI_OUTCOME', 'Complaint Confirmed', 1),
('JI_OUTCOME', 'Partially Confirmed', 2),
('JI_OUTCOME', 'Not Established', 3),
('ATTRIBUTABLE_TO', 'Manufacturing Defect', 1),
('ATTRIBUTABLE_TO', 'Transit Damage', 2),
('ATTRIBUTABLE_TO', 'Storage Issue', 3),
('ATTRIBUTABLE_TO', 'Handling Damage', 4),
('ATTRIBUTABLE_TO', 'Installation Issue', 5),
('ATTRIBUTABLE_TO', 'Material Mix-up', 6),
('ATTRIBUTABLE_TO', 'Not Attributable to RITES', 7),
('ATTRIBUTABLE_TO', 'Under Investigation', 8),
('ROOT_CAUSE', 'Manufacturing Process', 1),
('ROOT_CAUSE', 'Raw Material', 2),
('ROOT_CAUSE', 'Inspection Lapse', 3),
('ROOT_CAUSE', 'Packing', 4),
('ROOT_CAUSE', 'Transportation', 5),
('ROOT_CAUSE', 'Storage', 6),
('ROOT_CAUSE', 'Handling', 7),
('ROOT_CAUSE', 'Installation', 8),
('ROOT_CAUSE', 'Specification Issue', 9),
('ROOT_CAUSE', 'Others', 10),
('CORRECTIVE_ACTION', 'Vendor Instructed', 1),
('CORRECTIVE_ACTION', 'Material Replaced', 2),
('CORRECTIVE_ACTION', 'Re-inspection Conducted', 3),
('CORRECTIVE_ACTION', 'Additional Testing Done', 4),
('CORRECTIVE_ACTION', 'Lot Segregated', 5),
('CORRECTIVE_ACTION', 'Vendor Warning Issued', 6),
('CORRECTIVE_ACTION', 'Recovery Initiated', 7),
('CORRECTIVE_ACTION', 'Nil', 8),
('PREVENTIVE_ACTION', 'Increased Inspection Frequency', 1),
('PREVENTIVE_ACTION', 'Additional Process Audit', 2),
('PREVENTIVE_ACTION', 'Vendor Corrective Action', 3),
('PREVENTIVE_ACTION', 'Revised Inspection Checklist', 4),
('PREVENTIVE_ACTION', 'Additional Sample Testing', 5),
('PREVENTIVE_ACTION', 'Training Conducted', 6),
('PREVENTIVE_ACTION', 'Process Improvement', 7),
('PREVENTIVE_ACTION', 'Nil', 8),
('AUDIT_TYPE', 'Audit of Record', 1),
('AUDIT_TYPE', 'Audit of Firm', 2),
('AUDIT_TYPE', 'Both', 3);

COMMIT;

-- ---------------------------------------------------------------------
-- VERIFY (read only)
-- ---------------------------------------------------------------------
-- SELECT TRANSITIONNAME, CURRENTROLEID, NEXTROLEID FROM TRANSITION_MASTER WHERE WORKFLOWID = @UDM_WF ORDER BY TRANSITIONORDER;
-- SELECT category, COUNT(*) FROM udm_complaint_dropdown_master GROUP BY category;
-- SHOW CREATE TABLE udm_complaint_stage;
-- SHOW CREATE TABLE udm_complaint_document;
