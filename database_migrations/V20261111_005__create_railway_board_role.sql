-- =====================================================================
-- V20261111_005 : Create the "Railway Board" role (real users, not the mock login)
-- Needed for UDM complaint stage 7 (Railway Board approve / return) with a proper audit trail.
-- The existing mock login (user "Railwayboard", role RAILWAY_BOARD) is left untouched.
-- Safe to re-run (idempotent).
-- =====================================================================

START TRANSACTION;

INSERT INTO ROLE_MASTER (ROLENAME, CREATEDBY, CREATEDDATE)
SELECT 'Railway Board', 'Admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ROLE_MASTER WHERE ROLENAME = 'Railway Board');

COMMIT;

SET @RB_ROLE := (SELECT ROLEID FROM ROLE_MASTER WHERE ROLENAME = 'Railway Board' LIMIT 1);

-- VERIFY (read only) — note the ROLEID and share it (used by the Phase 3 transitions)
SELECT ROLEID, ROLENAME FROM ROLE_MASTER WHERE ROLENAME IN ('Railway Board', 'SBU Head', 'Complaint Cell');

-- ---------------------------------------------------------------------
-- ASSIGN USERS to Railway Board (edit USERID, then run)
-- The user must exist in USER_MASTER and log in with the normal SARTHI login.
-- ---------------------------------------------------------------------
-- INSERT INTO USER_ROLE_MASTER (USERID, ROLEID, CREATEDBY, CREATEDDATE)
-- SELECT <USERID>, @RB_ROLE, 'Admin', NOW()
-- FROM DUAL
-- WHERE NOT EXISTS (SELECT 1 FROM USER_ROLE_MASTER WHERE USERID = <USERID> AND ROLEID = @RB_ROLE);
