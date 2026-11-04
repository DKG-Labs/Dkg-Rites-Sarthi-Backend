-- Rail Pad: IBS vendor codes must be keyed by POI code in sarthi_ibs_poi_mapping
-- (call raising and the IBS API look them up by POI code, e.g. POI02).
-- Older Admin saves stored them under the plant ID (e.g. ':1007406/Mohali').
-- This script adds the missing POI rows from those plant-ID rows. Run each step and review the output.

-- STEP 1 (review): POIs with no IBS code, and the code found on their vendor's plant-ID rows
SELECT rpp.poi_code, rpp.company_name, rpp.unit_name, rpp.vendor_code,
       GROUP_CONCAT(DISTINCT pm.ibs_vendor_code) AS ibs_codes_from_plant_rows,
       COUNT(DISTINCT pm.ibs_vendor_code)        AS distinct_codes
FROM railpad_pincode_poi_mapping rpp
LEFT JOIN sarthi_ibs_poi_mapping pm
       ON pm.product_type = 'railpad'
      AND pm.poi_code NOT LIKE 'POI%'
      AND SUBSTRING_INDEX(REPLACE(TRIM(pm.poi_code), ':', ''), '/', 1) COLLATE utf8mb4_unicode_ci
          = REPLACE(TRIM(rpp.vendor_code), ':', '') COLLATE utf8mb4_unicode_ci
WHERE NOT EXISTS (
        SELECT 1 FROM sarthi_ibs_poi_mapping x
        WHERE x.product_type = 'railpad'
          AND x.poi_code COLLATE utf8mb4_unicode_ci = rpp.poi_code COLLATE utf8mb4_unicode_ci
          AND x.ibs_vendor_code IS NOT NULL AND x.ibs_vendor_code <> '')
GROUP BY rpp.poi_code, rpp.company_name, rpp.unit_name, rpp.vendor_code;
-- distinct_codes = 1 -> safe to copy (step 2). 0 -> enter the IBS code in Admin. >1 -> decide manually.

-- STEP 2 (insert): add a POI row where exactly one IBS code exists on the vendor's plant-ID rows
INSERT INTO sarthi_ibs_poi_mapping (poi_code, ibs_vendor_code, product_type, created_at, updated_at)
SELECT rpp.poi_code, MAX(pm.ibs_vendor_code), 'railpad', NOW(), NOW()
FROM railpad_pincode_poi_mapping rpp
JOIN sarthi_ibs_poi_mapping pm
  ON pm.product_type = 'railpad'
 AND pm.poi_code NOT LIKE 'POI%'
 AND pm.ibs_vendor_code IS NOT NULL AND pm.ibs_vendor_code <> ''
 AND SUBSTRING_INDEX(REPLACE(TRIM(pm.poi_code), ':', ''), '/', 1) COLLATE utf8mb4_unicode_ci
     = REPLACE(TRIM(rpp.vendor_code), ':', '') COLLATE utf8mb4_unicode_ci
WHERE rpp.poi_code IS NOT NULL AND rpp.poi_code <> ''
  AND NOT EXISTS (
        SELECT 1 FROM sarthi_ibs_poi_mapping x
        WHERE x.product_type = 'railpad'
          AND x.poi_code COLLATE utf8mb4_unicode_ci = rpp.poi_code COLLATE utf8mb4_unicode_ci)
GROUP BY rpp.poi_code
HAVING COUNT(DISTINCT pm.ibs_vendor_code) = 1;

-- STEP 3 (optional, later): once every Rail Pad POI has its row, the plant-ID rows are no longer needed.
-- Review first, then delete if you want a clean table:
-- SELECT * FROM sarthi_ibs_poi_mapping WHERE product_type = 'railpad' AND poi_code NOT LIKE 'POI%';
-- DELETE FROM sarthi_ibs_poi_mapping WHERE product_type = 'railpad' AND poi_code NOT LIKE 'POI%';
