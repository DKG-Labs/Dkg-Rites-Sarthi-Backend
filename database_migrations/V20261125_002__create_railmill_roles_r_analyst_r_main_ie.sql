-- =====================================================================
-- V20261125_002 : Rail-mill roles "R Analyst" and "R Main IE"
-- Rail-mill roles are stored with an "R " prefix and shown in the UI as "Rail ...":
--   R Process IE  (exists; role 25, still named 'Rail SMS' until V20261113_003 is run)
--                 -> SMS, Stage, NDT, Visual, Welding
--   R Main IE     -> everything R Process IE has + Testing, QCT, Calibration, Short Rail
--   R Analyst     -> reports only (Record: SMS Summary / SMS Heats, ISO Reports), no duty
-- ('Rail Process IE' / 'Rail Main IE' are the Rail Pad roles, hence the "R " names.)
-- Users are linked in USER_ROLE_MASTER and use the normal SARTHI login, which opens the rail-mill app (/sms).
-- Safe to re-run (idempotent).
-- =====================================================================

START TRANSACTION;

INSERT INTO ROLE_MASTER (ROLENAME, CREATEDBY, CREATEDDATE)
SELECT 'R Analyst', 'Admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ROLE_MASTER WHERE ROLENAME = 'R Analyst');

INSERT INTO ROLE_MASTER (ROLENAME, CREATEDBY, CREATEDDATE)
SELECT 'R Main IE', 'Admin', NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ROLE_MASTER WHERE ROLENAME = 'R Main IE');

COMMIT;

-- VERIFY (read only): expect R Analyst and R Main IE, plus R Process IE (or 'Rail SMS')
SELECT ROLEID, ROLENAME FROM ROLE_MASTER WHERE ROLENAME IN ('R Analyst', 'R Main IE', 'R Process IE', 'Rail SMS');

-- ---------------------------------------------------------------------
-- Assign users: Admin -> User Management (roles "Rail Analyst" / "Rail Main IE"), or here (edit USERID, then run)
-- ---------------------------------------------------------------------
-- SET @ROLE := (SELECT ROLEID FROM ROLE_MASTER WHERE ROLENAME = 'R Analyst' LIMIT 1);   -- or 'R Main IE'
-- INSERT INTO USER_ROLE_MASTER (USERID, ROLEID, CREATEDBY, CREATEDDATE)
-- SELECT <USERID>, @ROLE, 'Admin', NOW()
-- FROM DUAL
-- WHERE NOT EXISTS (SELECT 1 FROM USER_ROLE_MASTER WHERE USERID = <USERID> AND ROLEID = @ROLE);
