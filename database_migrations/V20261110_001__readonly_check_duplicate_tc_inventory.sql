-- READ-ONLY check (no data change).
-- New rule: TC Number must be unique system-wide in inventory_entries.
-- Lists TC numbers that already exist under more than one supplier or vendor
-- (rows of one TC = several heats of the same submission are expected and NOT listed).

SELECT
    LOWER(TRIM(tc_number))                         AS tc_number_normalized,
    COUNT(*)                                       AS heat_rows,
    COUNT(DISTINCT LOWER(TRIM(supplier_name)))     AS suppliers,
    COUNT(DISTINCT vendor_code)                    AS vendors,
    GROUP_CONCAT(DISTINCT supplier_name SEPARATOR ' | ') AS supplier_names,
    GROUP_CONCAT(DISTINCT vendor_code   SEPARATOR ' | ') AS vendor_codes,
    GROUP_CONCAT(id ORDER BY id)                   AS entry_ids
FROM inventory_entries
GROUP BY LOWER(TRIM(tc_number))
HAVING COUNT(DISTINCT LOWER(TRIM(supplier_name))) > 1
    OR COUNT(DISTINCT vendor_code) > 1
ORDER BY heat_rows DESC;
