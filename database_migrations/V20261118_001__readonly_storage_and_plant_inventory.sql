-- READ-ONLY inventory: units/plants per product, ICs and files stored, and Azure container usage.
-- Only SELECTs. The container is read from each stored blob URL (https://<account>.blob.core.windows.net/<container>/...).

-- ============ 1. Units / plants per product ============
SELECT 'ERC units'       AS item, COUNT(DISTINCT poi_code) AS total FROM pincode_poi_mapping
UNION ALL SELECT 'ERC units (active)', COUNT(DISTINCT poi_code) FROM pincode_poi_mapping WHERE UPPER(COALESCE(status,'')) IN ('ACTIVE','A','1','Y','YES')
UNION ALL SELECT 'Sleeper plants (POI mapping)', COUNT(DISTINCT poi_code) FROM sleeper_pincode_poi_mapping
UNION ALL SELECT 'Sleeper plants (vendor_plant)', COUNT(DISTINCT plant_id) FROM vendor_plant
UNION ALL SELECT 'Rail Pad plants (POI mapping)', COUNT(DISTINCT poi_code) FROM railpad_pincode_poi_mapping
UNION ALL SELECT 'Rail Pad plants (rail_vendor_plant)', COUNT(DISTINCT plant_id) FROM rail_vendor_plant;

-- ============ 2. Files stored, by table and container ============
SELECT src, container, COUNT(*) AS files
FROM (
    SELECT 'IC certificates (CERTIFICATE_STORAGE)' AS src, SUBSTRING_INDEX(SUBSTRING_INDEX(BLOB_URL, '.net/', -1), '/', 1) AS container FROM CERTIFICATE_STORAGE
    UNION ALL SELECT 'IC annexures',            SUBSTRING_INDEX(SUBSTRING_INDEX(BLOB_URL, '.net/', -1), '/', 1) FROM IC_ANNEXURE_DOCUMENT
    UNION ALL SELECT 'Case letters',            SUBSTRING_INDEX(SUBSTRING_INDEX(BLOB_URL, '.net/', -1), '/', 1) FROM CASE_LETTER_DOCUMENT
    UNION ALL SELECT 'Correction slips',        SUBSTRING_INDEX(SUBSTRING_INDEX(BLOB_URL, '.net/', -1), '/', 1) FROM CORRECTION_SLIP_DOCUMENT
    UNION ALL SELECT 'ERC inspection images',   SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM inspection_images
    UNION ALL SELECT 'Sleeper inspection photos', SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM sleeper_photo_inspection_records
    UNION ALL SELECT 'ERC scrap images',        SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM ie_scrap_entry_images
    UNION ALL SELECT 'Sleeper scrap images',    SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM sleeper_scrap_verification_image
    UNION ALL SELECT 'Rail Pad scrap images',   SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM rail_scrap_verification_image
    UNION ALL SELECT 'UDM complaint docs',      'sarthi-udm-complaint-docs' FROM udm_complaint_document
) x
GROUP BY src, container
ORDER BY src, files DESC;

-- ============ 3. Totals per container (all tables above) ============
SELECT container, COUNT(*) AS files
FROM (
    SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(BLOB_URL, '.net/', -1), '/', 1) AS container FROM CERTIFICATE_STORAGE
    UNION ALL SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(BLOB_URL, '.net/', -1), '/', 1) FROM IC_ANNEXURE_DOCUMENT
    UNION ALL SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(BLOB_URL, '.net/', -1), '/', 1) FROM CASE_LETTER_DOCUMENT
    UNION ALL SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(BLOB_URL, '.net/', -1), '/', 1) FROM CORRECTION_SLIP_DOCUMENT
    UNION ALL SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM inspection_images
    UNION ALL SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM sleeper_photo_inspection_records
    UNION ALL SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM ie_scrap_entry_images
    UNION ALL SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM sleeper_scrap_verification_image
    UNION ALL SELECT SUBSTRING_INDEX(SUBSTRING_INDEX(image_url, '.net/', -1), '/', 1) FROM rail_scrap_verification_image
    UNION ALL SELECT 'sarthi-udm-complaint-docs' FROM udm_complaint_document
) x
GROUP BY container
ORDER BY files DESC;

-- ============ 4. ICs stored per product (by IC / call number prefix) ============
SELECT LEFT(IC_NUMBER, LOCATE('-', CONCAT(IC_NUMBER, '-')) - 1) AS ic_prefix,
       COUNT(*) AS ics_stored, MIN(UPLOADED_AT) AS first_upload, MAX(UPLOADED_AT) AS last_upload
FROM CERTIFICATE_STORAGE
GROUP BY ic_prefix
ORDER BY ics_stored DESC;
