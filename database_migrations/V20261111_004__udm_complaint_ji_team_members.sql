-- =====================================================================
-- V20261111_004 : UDM Complaint — JI team picked from SARTHI users
-- JI team = selected IEs (ERC / Sleeper / Rail Pad, not Process IE) + CMs of the RIO,
-- plus optional other members (free text).
-- Run AFTER V20261111_003. Safe to re-run.
-- =====================================================================

-- 1) Longer JI team text (names of all members)
ALTER TABLE udm_complaint_stage MODIFY ji_team VARCHAR(1000) NULL;

-- 2) Selected member user ids (comma separated) — add only if missing
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'udm_complaint_stage' AND COLUMN_NAME = 'ji_team_user_ids'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE udm_complaint_stage ADD COLUMN ji_team_user_ids VARCHAR(500) NULL AFTER ji_team',
    'SELECT ''ji_team_user_ids already exists''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- VERIFY (read only)
-- SHOW COLUMNS FROM udm_complaint_stage LIKE 'ji_team%';
-- Role names that will be offered as IEs:
-- SELECT ROLEID, ROLENAME FROM ROLE_MASTER
-- WHERE LOWER(TRIM(ROLENAME)) IN ('ie','ie secondary','main ie','sleeper ie','sleeper main ie','rail main ie','railpad ie','railpad main ie','erc ie');
