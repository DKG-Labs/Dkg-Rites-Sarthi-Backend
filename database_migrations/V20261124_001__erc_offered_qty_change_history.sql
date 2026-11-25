-- =====================================================================
-- V20261124_001 : History of Super Admin "Change Offered Qty" (ERC ER / EP / EF)
-- One row per changed heat (ER) or lot (EP / EF). Safe to re-run.
-- =====================================================================

CREATE TABLE IF NOT EXISTS erc_offered_qty_change_history (
  id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
  call_no             VARCHAR(50)   NOT NULL,
  call_type           VARCHAR(5)    NOT NULL,          -- ER / EP / EF
  row_ref             VARCHAR(255)  NULL,              -- e.g. "Heat H123 · TC 45" or "Lot 7 · Heat H123"
  heat_no             VARCHAR(100)  NULL,
  lot_no              VARCHAR(100)  NULL,
  old_qty             DECIMAL(15,3) NOT NULL,
  new_qty             DECIMAL(15,3) NOT NULL,
  unit                VARCHAR(10)   NOT NULL,          -- MT (ER) / Nos (EP, EF)
  status_at_change    VARCHAR(100)  NULL,
  remarks             VARCHAR(1000) NOT NULL,
  changed_by          INT           NOT NULL,
  changed_by_emp_code VARCHAR(50)   NULL,
  changed_by_name     VARCHAR(150)  NULL,
  changed_at          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tables_updated      TEXT          NULL,              -- JSON summary of rows updated per table
  KEY idx_eoqch_call (call_no),
  KEY idx_eoqch_changed_at (changed_at)
);
