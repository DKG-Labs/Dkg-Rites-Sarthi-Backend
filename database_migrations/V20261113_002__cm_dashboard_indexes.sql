-- ============================================================================
-- CM Dashboard performance: indexes for the per-row lookups in the
-- cm-inspection-calls / cm-*-overdue reports (ERC, Sleeper, Rail Pad).
-- Safe to re-run: each index is created only if it does not already exist.
-- Run manually on each DB (local / QA / UAT / PROD). No data is changed.
-- ============================================================================

DROP PROCEDURE IF EXISTS sarthi_add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE sarthi_add_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_cols VARCHAR(255))
BEGIN
    -- first column of the requested index, e.g. 'requestid' from 'requestid, workflowtransitionid'
    DECLARE v_first VARCHAR(64) DEFAULT TRIM(SUBSTRING_INDEX(p_cols, ',', 1));

    -- Skip when: table/column missing, column is TEXT/BLOB (cannot be indexed without a length),
    -- an index with this name exists, or ANY index already starts with this column (avoids duplicates).
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = v_first
                 AND data_type NOT IN ('text','tinytext','mediumtext','longtext','blob','tinyblob','mediumblob','longblob','json'))
       AND NOT EXISTS (SELECT 1 FROM information_schema.statistics
                       WHERE table_schema = DATABASE() AND table_name = p_table
                         AND (index_name = p_index OR (seq_in_index = 1 AND column_name = v_first))) THEN
        -- Online build: table stays readable/writable; fails instead of locking if online DDL isn't possible
        SET @ddl = CONCAT('CREATE INDEX ', p_index, ' ON ', p_table, ' (', p_cols, ') ALGORITHM=INPLACE LOCK=NONE');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- Latest workflow row per call ("SELECT MAX(id) ... WHERE request_id = call_no")
CALL sarthi_add_index_if_missing('workflow_transition',          'idx_wt_req_id',        'requestid, workflowtransitionid');
CALL sarthi_add_index_if_missing('sleeper_workflow_transaction', 'idx_swt_req_id',       'request_id, workflow_transition_id');
CALL sarthi_add_index_if_missing('rail_workflow_transaction',    'idx_rwt_req_id',       'request_id, workflow_transition_id');

-- Date-range filter on the call tables
CALL sarthi_add_index_if_missing('inspection_calls',             'idx_ic_created_at',    'created_at');
CALL sarthi_add_index_if_missing('sleeper_inspection_call',      'idx_sic_created_at',   'created_at');
CALL sarthi_add_index_if_missing('rail_inspection_call',         'idx_rc_created_at',    'created_at');

-- POI / plant -> IE
CALL sarthi_add_index_if_missing('ie_pincode_poi_mapping',       'idx_ipm_poi',          'poi_code');
CALL sarthi_add_index_if_missing('poi_process_ie_mapping',       'idx_ppim_poi',         'poi_code');
CALL sarthi_add_index_if_missing('sleeper_poi_ie_mapping',       'idx_spim_plant_type',  'plant_id, ie_type');
CALL sarthi_add_index_if_missing('rail_poi_ie_mapping',          'idx_rpm_plant_type',   'plant_id, ie_type');

-- IE -> CM, names, RIO, vendor
CALL sarthi_add_index_if_missing('user_master',                  'idx_um_employee_code', 'employee_code');
CALL sarthi_add_index_if_missing('user_master',                  'idx_um_cm_id',         'controlling_manager_id');
CALL sarthi_add_index_if_missing('user_product_cm_mapping',      'idx_upcm_user_prod',   'user_employee_code, product_type');
CALL sarthi_add_index_if_missing('pincode_poi_mapping',          'idx_ppm_poi',          'poi_code');
CALL sarthi_add_index_if_missing('sleeper_pincode_poi_mapping',  'idx_sppm_vendor',      'vendor_code');
CALL sarthi_add_index_if_missing('ie_fields_mapping',            'idx_ifm_pin_product',  'pin_code, product');
CALL sarthi_add_index_if_missing('po_header',                    'idx_ph_po_no',         'po_no');

DROP PROCEDURE IF EXISTS sarthi_add_index_if_missing;

-- ----------------------------------------------------------------------------
-- Optional check (read-only): mixed collations stop MySQL from using indexes
-- when tables are joined. Any table listed here differs from utf8mb4_unicode_ci.
-- Converting them (ALTER TABLE ... CONVERT TO CHARACTER SET utf8mb4 COLLATE
-- utf8mb4_unicode_ci) is the long-term fix — do that separately, off-peak.
-- ----------------------------------------------------------------------------
SELECT table_name, table_collation
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN ('inspection_calls','workflow_transition','sleeper_inspection_call','sleeper_workflow_transaction',
                     'rail_inspection_call','rail_workflow_transaction','user_master','user_product_cm_mapping',
                     'ie_pincode_poi_mapping','poi_process_ie_mapping','sleeper_poi_ie_mapping','rail_poi_ie_mapping',
                     'pincode_poi_mapping','sleeper_pincode_poi_mapping','ie_fields_mapping','po_header')
  AND table_collation <> 'utf8mb4_unicode_ci';
