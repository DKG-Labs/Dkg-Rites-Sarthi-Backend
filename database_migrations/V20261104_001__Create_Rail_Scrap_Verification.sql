-- Rail Pad Rejection Basket & Scrap Verification (SRS - Rejection basket)
-- Rejections are read live from inspection tables; these tables store only the
-- scrap verifications (ledger debits), their photos and the edit/delete audit trail.

-- 1. Scrap verification (debit entries of the scrap ledger)
CREATE TABLE rail_scrap_verification (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  verification_no        VARCHAR(30)  NOT NULL,                  -- RSV-yyMMdd-NN
  plant_id               VARCHAR(100) NOT NULL,                  -- rail_vendor_plant.plant_id (normalised, no ':')
  vendor_code            VARCHAR(100) NULL,
  company_name           VARCHAR(255) NULL,                      -- snapshot for reports
  plant_name             VARCHAR(255) NULL,
  product_type           VARCHAR(100) NOT NULL,                  -- e.g. 6.2mm CGRSP
  scrapping_date         DATE         NOT NULL,
  shift                  VARCHAR(20)  NULL,
  available_pending_qty  INT          NOT NULL,                  -- pending balance seen at submit
  qty_scrapped           INT          NOT NULL,                  -- pieces witnessed for scrapping
  declaration_accepted   TINYINT(1)   NOT NULL DEFAULT 0,
  declaration_text       VARCHAR(500) NOT NULL,
  remarks                TEXT         NULL,
  ie_user_id             BIGINT       NOT NULL,
  ie_name                VARCHAR(255) NULL,
  ie_type                VARCHAR(20)  NOT NULL,                  -- PROCESS_IE
  status                 VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / DELETED (soft delete)
  created_by             VARCHAR(50)  NULL,
  created_date           DATETIME(6)  NOT NULL,
  updated_by             VARCHAR(50)  NULL,
  updated_date           DATETIME(6)  NULL,
  UNIQUE KEY uq_rsv_verification_no (verification_no),
  KEY idx_rsv_plant_type_status (plant_id, product_type, status),
  KEY idx_rsv_scrapping_date (scrapping_date),
  CONSTRAINT chk_rsv_qty_positive CHECK (qty_scrapped > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Before / after destruction photos
CREATE TABLE rail_scrap_verification_image (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  scrap_verification_id  BIGINT       NOT NULL,
  photo_type             VARCHAR(10)  NOT NULL,                  -- BEFORE / AFTER
  image_url              VARCHAR(500) NOT NULL,
  blob_name              VARCHAR(255) NOT NULL,
  created_by             VARCHAR(50)  NULL,
  created_date           DATETIME(6)  NOT NULL,
  KEY idx_rsvi_verification (scrap_verification_id, photo_type),
  CONSTRAINT fk_rsvi_verification FOREIGN KEY (scrap_verification_id)
    REFERENCES rail_scrap_verification (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Immutable audit trail of edits / deletes (allowed only within 1 hour of creation)
CREATE TABLE rail_scrap_verification_audit (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  scrap_verification_id  BIGINT       NOT NULL,
  action                 VARCHAR(10)  NOT NULL,                  -- EDIT / DELETE
  field_changed          VARCHAR(100) NULL,
  old_value              TEXT         NULL,
  new_value              TEXT         NULL,
  reason                 TEXT         NOT NULL,
  action_by              VARCHAR(50)  NOT NULL,
  action_at              DATETIME(6)  NOT NULL,
  KEY idx_rsva_verification (scrap_verification_id, action_at),
  CONSTRAINT fk_rsva_verification FOREIGN KEY (scrap_verification_id)
    REFERENCES rail_scrap_verification (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Indexes on existing tables so the live basket queries stay fast.
--    Run SHOW INDEX FROM <table> first and skip any that already exist.
CREATE INDEX idx_rie_prod_ver_unit_date ON rail_ie_production_verification (production_unit, casting_date);
CREATE INDEX idx_rfilr_plant_status_date ON rail_final_inspection_lot_results (plant_id, overall_status, date_of_inspection);
