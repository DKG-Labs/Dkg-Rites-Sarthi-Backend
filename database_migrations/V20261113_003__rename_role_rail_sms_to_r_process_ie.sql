-- Rename role 'Rail SMS' -> 'R Process IE' (shown in the UI as 'Rail Process IE').
-- 'Rail Process IE' is already used by the Rail Pad Process IE role, so the SMS role is stored as 'R Process IE'.
-- Users keep their role: user_role_master links by ROLEID, not by name.

-- 1. Check (read-only)
SELECT ROLEID, ROLENAME FROM ROLE_MASTER WHERE ROLENAME IN ('Rail SMS', 'R Process IE', 'Rail Process IE');

-- 2. Rename
START TRANSACTION;
-- Safe update mode needs the key column: put the ROLEID returned by query 1 for 'Rail SMS'
UPDATE ROLE_MASTER SET ROLENAME = 'R Process IE' WHERE ROLEID = 25 AND ROLENAME = 'Rail SMS';
SELECT ROLEID, ROLENAME FROM ROLE_MASTER WHERE ROLENAME IN ('Rail SMS', 'R Process IE');
-- Expect 1 row changed, then:
-- COMMIT;   -- or ROLLBACK;
