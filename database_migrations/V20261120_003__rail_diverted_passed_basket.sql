-- Rail Pad diversion: the only table of V20261118_003 missing in prod (checked 20/11/2026).
-- The other 4 tables and rail_inspection_call.call_category already exist.

CREATE TABLE IF NOT EXISTS rail_diverted_passed_basket (
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

-- Check
SELECT COUNT(*) FROM rail_diverted_passed_basket;
