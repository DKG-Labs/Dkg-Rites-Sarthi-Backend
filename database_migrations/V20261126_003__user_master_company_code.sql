-- Company Code on users: Regular / Contractual = same as employee code, MPA = entered manually.
-- Run BEFORE deploying the backend (the user list query selects this column).

ALTER TABLE user_master ADD COLUMN COMPANY_CODE VARCHAR(50) NULL AFTER EMPLOYMENT_TYPE;

-- Fill existing Regular / Contractual employees with their employee code
-- (vendors and Zonal Railway users are left empty; MPA users get theirs entered from the form)
UPDATE user_master
SET COMPANY_CODE = EMPLOYEE_CODE
WHERE userid > 0   -- key column, needed for Workbench safe update mode
  AND COMPANY_CODE IS NULL
  AND UPPER(TRIM(EMPLOYMENT_TYPE)) IN ('REGULAR', 'CONTRACTUAL', 'CONTRACT', 'PERMANENT');

-- check
SELECT EMPLOYMENT_TYPE, COUNT(*) total, SUM(COMPANY_CODE IS NOT NULL) with_company_code
FROM user_master GROUP BY EMPLOYMENT_TYPE;
