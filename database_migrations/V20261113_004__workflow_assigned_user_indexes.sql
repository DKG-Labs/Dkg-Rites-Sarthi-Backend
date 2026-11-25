-- ============================================================================
-- CM Dashboard: the CM's calls are now found ONCE per request as
-- "workflow rows assigned to this CM's IEs" -> needs an index on assigned_to_user.
-- Safe to re-run (skips existing / duplicate / missing). No data is changed.
-- Run manually on each DB, off-peak. Same helper as V20261113_002.
-- ============================================================================

DROP PROCEDURE IF EXISTS sarthi_add_index_if_missing;
DELIMITER $$
CREATE PROCEDURE sarthi_add_index_if_missing(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_cols VARCHAR(255))
BEGIN
    DECLARE v_first VARCHAR(64) DEFAULT TRIM(SUBSTRING_INDEX(p_cols, ',', 1));
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = v_first
                 AND data_type NOT IN ('text','tinytext','mediumtext','longtext','blob','tinyblob','mediumblob','longblob','json'))
       AND NOT EXISTS (SELECT 1 FROM information_schema.statistics
                       WHERE table_schema = DATABASE() AND table_name = p_table
                         AND (index_name = p_index OR (seq_in_index = 1 AND column_name = v_first))) THEN
        SET @ddl = CONCAT('CREATE INDEX ', p_index, ' ON ', p_table, ' (', p_cols, ') ALGORITHM=INPLACE LOCK=NONE');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- "Which calls were assigned to these IEs?" (one lookup per CM, then joined to the call list)
CALL sarthi_add_index_if_missing('workflow_transition',          'idx_wt_assigned_req',  'assigned_to_user, requestid');
CALL sarthi_add_index_if_missing('sleeper_workflow_transaction', 'idx_swt_assigned_req', 'assigned_to_user, request_id');
CALL sarthi_add_index_if_missing('rail_workflow_transaction',    'idx_rwt_assigned_req', 'assigned_to_user, request_id');

-- Call number lookups (join from the CM's call list back to the call)
CALL sarthi_add_index_if_missing('inspection_calls',             'idx_ic_ic_number',     'ic_number');
CALL sarthi_add_index_if_missing('sleeper_inspection_call',      'idx_sic_call_no',      'call_no');

DROP PROCEDURE IF EXISTS sarthi_add_index_if_missing;

-- Check (read-only): confirms the indexes the dashboard relies on are present
SELECT table_name, index_name, GROUP_CONCAT(column_name ORDER BY seq_in_index) AS cols
FROM information_schema.statistics
WHERE table_schema = DATABASE()
  AND table_name IN ('workflow_transition','sleeper_workflow_transaction','rail_workflow_transaction',
                     'inspection_calls','sleeper_inspection_call','user_master')
  AND column_name IN ('requestid','request_id','assigned_to_user','ic_number','call_no','controlling_manager_id')
GROUP BY table_name, index_name
ORDER BY table_name, index_name;
