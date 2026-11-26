-- UAT only (sarthibackenddb). Do NOT run on prod.
-- 104937 (suryapraskash): region WRIO -> NRIO
-- 851980 (Makam Jaya Kishore): mobile number -> NULL

UPDATE user_master SET Region = 'NRIO' WHERE EMPLOYEE_CODE = '104937';
UPDATE user_master SET mobilenumber = NULL WHERE EMPLOYEE_CODE = '851980';

-- check
SELECT userid, EMPLOYEE_CODE, FULL_NAME, Region, mobilenumber
FROM user_master WHERE EMPLOYEE_CODE IN ('104937', '851980');
