-- Rail Pad: keep a copy of IE production batch rows (rail_ie_production_info) before they are deleted
-- by Unblock, re-save of the verification, or delete. Unblock is NOT blocked; this only keeps history.
-- deleted_by = logged-in user id (from the login token, not from the request).

CREATE TABLE IF NOT EXISTS rail_ie_production_info_history (
  id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
  action                VARCHAR(20)  NOT NULL,          -- UNBLOCK / RESAVE / DELETE
  unblock_history_id    BIGINT       NULL,              -- Rail_unblock_workflow_hitory.id (for UNBLOCK)
  info_id               BIGINT       NOT NULL,          -- old rail_ie_production_info.id (used as declarationBatchId)
  verification_id       BIGINT       NULL,
  request_id            BIGINT       NULL,              -- rail_production_declaration.id
  production_unit       VARCHAR(100) NULL,
  casting_date          DATE         NULL,
  shift                 VARCHAR(50)  NULL,
  product_type          VARCHAR(100) NULL,
  drawing_no            VARCHAR(100) NULL,
  batch_no              VARCHAR(100) NULL,
  initial_wt            DOUBLE       NULL,
  final_wt              DOUBLE       NULL,
  quantity_produced     INT          NULL,
  used_accepted_qty     INT          NOT NULL DEFAULT 0, -- accepted in Process calls against this id
  used_rejected_qty     INT          NOT NULL DEFAULT 0,
  used_in_calls         VARCHAR(1000) NULL,             -- e.g. "RPP-101626001:440"
  deleted_by            INT          NULL,              -- user_master.user_id
  deleted_by_name       VARCHAR(150) NULL,
  deleted_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_ripih_info (info_id),
  KEY idx_ripih_request (request_id),
  KEY idx_ripih_batch (production_unit, casting_date, batch_no)
);

-- Check
SELECT COUNT(*) FROM rail_ie_production_info_history;
