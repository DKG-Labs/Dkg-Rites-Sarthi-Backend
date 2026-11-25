-- READ-ONLY (SELECT only): users, roles, ERC units, Sleeper and Rail Pad plants. Safe to run on production.

-- ===== 1. Users =====
SELECT COUNT(*)                                              AS total_users,
       SUM(LAST_LOGIN_DATE IS NOT NULL)                      AS ever_logged_in,
       SUM(LAST_LOGIN_DATE >= NOW() - INTERVAL 30 DAY)       AS active_last_30_days,
       SUM(NOT EXISTS (SELECT 1 FROM USER_ROLE_MASTER r WHERE r.USERID = u.USERID)) AS users_without_role
FROM USER_MASTER u;

-- ===== 2. Users per role (a user with 2 roles is counted in both) =====
SELECT rm.ROLEID,
       rm.ROLENAME,
       COUNT(DISTINCT urm.USERID)                                              AS users,
       COUNT(DISTINCT CASE WHEN u.LAST_LOGIN_DATE >= NOW() - INTERVAL 30 DAY THEN u.USERID END) AS active_30_days
FROM ROLE_MASTER rm
LEFT JOIN USER_ROLE_MASTER urm ON urm.ROLEID = rm.ROLEID
LEFT JOIN USER_MASTER u        ON u.USERID = urm.USERID
GROUP BY rm.ROLEID, rm.ROLENAME
ORDER BY users DESC, rm.ROLENAME;

-- ===== 3. Users with more than one role =====
SELECT COUNT(*) AS users_with_multiple_roles
FROM (SELECT USERID FROM USER_ROLE_MASTER GROUP BY USERID HAVING COUNT(DISTINCT ROLEID) > 1) x;

-- ===== 4. Units / plants =====
SELECT 'ERC units (pincode_poi_mapping)'           AS item, COUNT(DISTINCT poi_code) AS total, COUNT(DISTINCT company_name) AS companies FROM pincode_poi_mapping
UNION ALL
SELECT 'Sleeper plants (sleeper_pincode_poi_mapping)', COUNT(DISTINCT poi_code), COUNT(DISTINCT company_name) FROM sleeper_pincode_poi_mapping
UNION ALL
SELECT 'Sleeper plants (vendor_plant)',             COUNT(DISTINCT plant_id), COUNT(DISTINCT company_name) FROM vendor_plant
UNION ALL
SELECT 'Rail Pad plants (railpad_pincode_poi_mapping)', COUNT(DISTINCT poi_code), COUNT(DISTINCT company_name) FROM railpad_pincode_poi_mapping
UNION ALL
SELECT 'Rail Pad plants (rail_vendor_plant)',       COUNT(DISTINCT plant_id), COUNT(DISTINCT company_name) FROM rail_vendor_plant;

-- ===== 5. Units / plants by status (if the status column is used) =====
SELECT 'ERC' AS product, COALESCE(NULLIF(TRIM(status), ''), '(blank)') AS status, COUNT(DISTINCT poi_code) AS units FROM pincode_poi_mapping GROUP BY 2
UNION ALL
SELECT 'Sleeper', COALESCE(NULLIF(TRIM(status), ''), '(blank)'), COUNT(DISTINCT poi_code) FROM sleeper_pincode_poi_mapping GROUP BY 2
UNION ALL
SELECT 'Rail Pad', COALESCE(NULLIF(TRIM(status), ''), '(blank)'), COUNT(DISTINCT poi_code) FROM railpad_pincode_poi_mapping GROUP BY 2
ORDER BY product, units DESC;
