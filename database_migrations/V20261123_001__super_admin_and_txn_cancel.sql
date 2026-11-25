-- =====================================================================
-- V20261123_001 : Super Admin role + "Cancel Last Transaction" audit columns
-- Safe to re-run (idempotent).  Run on UAT first, then PROD.
-- =====================================================================

START TRANSACTION;

-- 1. Super Admin role
INSERT INTO role_master (rolename, createdby, createddate)
SELECT 'Super Admin', 'Admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM role_master WHERE rolename = 'Super Admin');

COMMIT;

-- 2. Who cancelled a transaction, and why (added to the existing revert history table)
SET @db := DATABASE();

SET @s := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='workflow_delete_history' AND COLUMN_NAME='DELETED_BY_EMP_CODE')=0,
  'ALTER TABLE workflow_delete_history ADD COLUMN DELETED_BY_EMP_CODE VARCHAR(50) NULL', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='workflow_delete_history' AND COLUMN_NAME='DELETED_BY_NAME')=0,
  'ALTER TABLE workflow_delete_history ADD COLUMN DELETED_BY_NAME VARCHAR(150) NULL', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='workflow_delete_history' AND COLUMN_NAME='DELETE_REMARKS')=0,
  'ALTER TABLE workflow_delete_history ADD COLUMN DELETE_REMARKS VARCHAR(1000) NULL', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='workflow_delete_history' AND COLUMN_NAME='DELETE_SOURCE')=0,
  'ALTER TABLE workflow_delete_history ADD COLUMN DELETE_SOURCE VARCHAR(30) NULL', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s := IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='workflow_delete_history' AND COLUMN_NAME='CLEANUP_SUMMARY')=0,
  'ALTER TABLE workflow_delete_history ADD COLUMN CLEANUP_SUMMARY TEXT NULL', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

SET @s := IF((SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='workflow_delete_history' AND INDEX_NAME='idx_wdh_source_request')=0,
  'CREATE INDEX idx_wdh_source_request ON workflow_delete_history (DELETE_SOURCE, REQUEST_ID)', 'SELECT 1');
PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;

-- VERIFY (read only)
SELECT roleid, rolename FROM role_master WHERE rolename = 'Super Admin';

-- ---------------------------------------------------------------------
-- ASSIGN a user to Super Admin (edit <USERID>, then run)
-- ---------------------------------------------------------------------
-- INSERT INTO user_role_master (userid, roleid, createdby, createddate, readpermission, writepermission)
-- SELECT <USERID>, r.roleid, 'Admin', NOW(), b'1', b'1' FROM role_master r
-- WHERE r.rolename = 'Super Admin'
--   AND NOT EXISTS (SELECT 1 FROM user_role_master u WHERE u.userid = <USERID> AND u.roleid = r.roleid);
