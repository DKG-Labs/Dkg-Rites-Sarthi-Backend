-- Rail Pad Material Diversion (mirrors ERC erc_material_diversion_*; Process and Final stages only).
-- Run once per environment. Adds new tables + nullable columns on rail_inspection_call (existing data unaffected).

CREATE TABLE rail_material_diversion_request (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  request_no VARCHAR(50) NOT NULL,
  diversion_no VARCHAR(30) NULL,
  vendor_code VARCHAR(50) NOT NULL,
  company_name VARCHAR(150) NULL,
  plant_id VARCHAR(100) NOT NULL,              -- Rail Pad plant id is text (e.g. 1149316/Faridabad)
  plant_name VARCHAR(150) NULL,
  plant_address VARCHAR(500) NULL,
  rio_id VARCHAR(20) NOT NULL,
  cm_user_id VARCHAR(50) NOT NULL,
  sbu_head_user_id VARCHAR(50) NULL,
  stage VARCHAR(20) NOT NULL,                  -- PROCESS / FINAL
  diversion_type VARCHAR(20) NOT NULL,         -- PARTIAL / COMPLETE
  source_po_no VARCHAR(50) NOT NULL,
  source_po_sr_no VARCHAR(20) NOT NULL,
  source_ic_no VARCHAR(100) NOT NULL,
  source_call_no VARCHAR(50) NOT NULL,
  source_ic_date DATE NULL,
  source_ie_name VARCHAR(150) NULL,
  source_ie_user_id VARCHAR(50) NULL,
  target_po_no VARCHAR(50) NOT NULL,
  target_po_sr_no VARCHAR(20) NOT NULL,
  total_diversion_qty DECIMAL(14,3) NOT NULL,
  unit_of_measurement VARCHAR(20) NULL,        -- Nos / Sets (NCRGRSP)
  reason_code VARCHAR(50) NULL,
  reason_remarks TEXT NULL,
  railway_permission_no VARCHAR(100) NOT NULL,
  railway_permission_date DATE NOT NULL,
  railway_permission_doc_url LONGTEXT NOT NULL,
  vendor_remarks TEXT NULL,
  status VARCHAR(30) NOT NULL,
  diverted_call_no VARCHAR(50) NULL,           -- the diverted-passed RPP/RPF call created on approval
  created_by VARCHAR(50) NULL,
  created_date DATETIME NOT NULL,
  updated_by VARCHAR(50) NULL,
  updated_date DATETIME NULL,
  UNIQUE KEY uk_rmdr_request_no (request_no),
  UNIQUE KEY uk_rmdr_diversion_no (diversion_no),
  KEY idx_rmdr_vendor (vendor_code, status),
  KEY idx_rmdr_status (status),
  KEY idx_rmdr_cm (cm_user_id, status),
  KEY idx_rmdr_rio (rio_id, status),
  KEY idx_rmdr_source_ic (source_ic_no),
  KEY idx_rmdr_target (target_po_no, target_po_sr_no)
);

CREATE TABLE rail_material_diversion_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  diversion_request_id BIGINT NOT NULL,
  lot_no VARCHAR(50) NULL,
  batch_no VARCHAR(50) NULL,
  accepted_qty DECIMAL(14,3) NOT NULL,
  downstream_consumed_qty DECIMAL(14,3) NOT NULL DEFAULT 0,
  available_balance_qty DECIMAL(14,3) NOT NULL,
  diversion_qty DECIMAL(14,3) NOT NULL,
  remaining_balance_qty DECIMAL(14,3) NOT NULL,
  created_by VARCHAR(50) NULL,
  created_date DATETIME NOT NULL,
  updated_by VARCHAR(50) NULL,
  updated_date DATETIME NULL,
  KEY idx_rmdi_request (diversion_request_id),
  CONSTRAINT fk_rmdi_request FOREIGN KEY (diversion_request_id) REFERENCES rail_material_diversion_request(id)
);

CREATE TABLE rail_diverted_passed_basket (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  diversion_request_id BIGINT NOT NULL,
  diversion_item_id BIGINT NOT NULL,
  stage VARCHAR(20) NOT NULL,
  vendor_code VARCHAR(50) NOT NULL,
  plant_id VARCHAR(100) NOT NULL,
  rio_id VARCHAR(20) NOT NULL,
  source_po_no VARCHAR(50) NOT NULL,
  source_po_sr_no VARCHAR(20) NOT NULL,
  source_ic_no VARCHAR(100) NOT NULL,
  diverted_call_no VARCHAR(50) NULL,           -- diverted-passed call (its IC is what downstream diverted calls use)
  target_po_no VARCHAR(50) NOT NULL,
  target_po_sr_no VARCHAR(20) NOT NULL,
  lot_no VARCHAR(50) NULL,
  batch_no VARCHAR(50) NULL,
  approved_diverted_qty DECIMAL(14,3) NOT NULL,
  allocated_call_qty DECIMAL(14,3) NOT NULL DEFAULT 0,
  consumed_ic_qty DECIMAL(14,3) NOT NULL DEFAULT 0,
  available_balance_qty DECIMAL(14,3) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',   -- AVAILABLE / LOCKED / EXHAUSTED
  version BIGINT NOT NULL DEFAULT 0,                 -- optimistic lock
  created_by VARCHAR(50) NULL,
  created_date DATETIME NOT NULL,
  updated_by VARCHAR(50) NULL,
  updated_date DATETIME NULL,
  KEY idx_rdpb_request (diversion_request_id),
  KEY idx_rdpb_vendor (vendor_code, status),
  KEY idx_rdpb_target (target_po_no, target_po_sr_no, stage),
  KEY idx_rdpb_diverted_call (diverted_call_no),
  CONSTRAINT fk_rdpb_request FOREIGN KEY (diversion_request_id) REFERENCES rail_material_diversion_request(id),
  CONSTRAINT fk_rdpb_item FOREIGN KEY (diversion_item_id) REFERENCES rail_material_diversion_item(id)
);

CREATE TABLE rail_diversion_call_allocation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  call_no VARCHAR(50) NOT NULL,
  basket_id BIGINT NOT NULL,
  allocated_qty DECIMAL(14,3) NOT NULL,
  consumed_qty DECIMAL(14,3) NOT NULL DEFAULT 0,
  status VARCHAR(30) NOT NULL DEFAULT 'RESERVED',    -- RESERVED / RELEASED / CONSUMED_IN_IC
  created_by VARCHAR(50) NULL,
  created_date DATETIME NOT NULL,
  updated_by VARCHAR(50) NULL,
  updated_date DATETIME NULL,
  KEY idx_rdca_call (call_no, status),
  KEY idx_rdca_basket (basket_id, status),
  CONSTRAINT fk_rdca_basket FOREIGN KEY (basket_id) REFERENCES rail_diverted_passed_basket(id)
);

CREATE TABLE rail_diversion_workflow_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  diversion_request_id BIGINT NOT NULL,
  action_role VARCHAR(30) NOT NULL,
  action_user_id VARCHAR(50) NOT NULL,
  action_user_name VARCHAR(100) NULL,
  action VARCHAR(40) NOT NULL,
  from_status VARCHAR(30) NULL,
  to_status VARCHAR(30) NOT NULL,
  remarks TEXT NULL,
  created_by VARCHAR(50) NULL,
  created_date DATETIME NOT NULL,
  updated_by VARCHAR(50) NULL,
  updated_date DATETIME NULL,
  KEY idx_rdwh_request (diversion_request_id),
  CONSTRAINT fk_rdwh_request FOREIGN KEY (diversion_request_id) REFERENCES rail_material_diversion_request(id)
);

-- Diversion flags on Rail Pad calls (nullable; existing calls stay FRESH)
ALTER TABLE rail_inspection_call
  ADD COLUMN call_nature VARCHAR(20) NULL DEFAULT 'FRESH',      -- FRESH / DIVERTED
  ADD COLUMN call_category VARCHAR(30) NULL DEFAULT 'FRESH',    -- FRESH / DIVERTED / DIVERTED_PASSED
  ADD COLUMN is_diverted_material TINYINT(1) NULL DEFAULT 0,
  ADD COLUMN diversion_request_no VARCHAR(50) NULL,
  ADD COLUMN source_ic_no VARCHAR(100) NULL;

CREATE INDEX idx_ric_source_ic ON rail_inspection_call (source_ic_no);
CREATE INDEX idx_ric_diversion_req ON rail_inspection_call (diversion_request_no);
CREATE INDEX idx_ric_call_category ON rail_inspection_call (call_category);
