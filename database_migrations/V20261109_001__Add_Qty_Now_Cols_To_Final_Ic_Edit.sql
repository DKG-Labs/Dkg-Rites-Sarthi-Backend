-- ERC Final IC: store edited columns 6-8 (Qty now offered / passed / rejected) in Save Changes and IC Edit.
-- Column 9 (Qty still due) already exists as QTY_STILL_DUE.
-- MySQL 8 has no "ADD COLUMN IF NOT EXISTS", so each column is added only if missing (safe to re-run).

DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing(IN p_table VARCHAR(64), IN p_column VARCHAR(64), IN p_def VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND UPPER(TABLE_NAME) = UPPER(p_table)
          AND UPPER(COLUMN_NAME) = UPPER(p_column)
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE ', p_table, ' ADD COLUMN ', p_column, ' ', p_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

CALL add_col_if_missing('FINAL_IC_SAVE_CHANGES', 'QTY_NOW_OFFERED',  'VARCHAR(255) NULL');
CALL add_col_if_missing('FINAL_IC_SAVE_CHANGES', 'QTY_NOW_PASSED',   'VARCHAR(255) NULL');
CALL add_col_if_missing('FINAL_IC_SAVE_CHANGES', 'QTY_NOW_REJECTED', 'VARCHAR(255) NULL');

CALL add_col_if_missing('FINAL_IC_EDIT', 'QTY_NOW_OFFERED',  'VARCHAR(255) NULL');
CALL add_col_if_missing('FINAL_IC_EDIT', 'QTY_NOW_PASSED',   'VARCHAR(255) NULL');
CALL add_col_if_missing('FINAL_IC_EDIT', 'QTY_NOW_REJECTED', 'VARCHAR(255) NULL');

DROP PROCEDURE IF EXISTS add_col_if_missing;
