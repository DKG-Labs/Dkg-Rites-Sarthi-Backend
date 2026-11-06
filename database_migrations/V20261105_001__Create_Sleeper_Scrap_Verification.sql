-- Concrete Sleeper Rejection Basket & Scrap Verification (SRS - Rejection Basket & Scrap Verification)
-- Rejected sleepers are read live from demoulding_defective_sleepers and the final inspection rejection
-- tables; these tables store only the scrap verifications (ledger debits), the exact sleepers each one
-- destroyed, their photos and the edit/delete audit trail.

-- 1. Scrap verification (debit entries of the scrap ledger)
CREATE TABLE sleeper_scrap_verification (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  verification_no        VARCHAR(30)  NOT NULL,                  -- SSV-yyMMdd-NN
  plant_id               VARCHAR(100) NOT NULL,                  -- vendor_plant.plant_id (normalised, no ':')
  vendor_code            VARCHAR(100) NULL,
  company_name           VARCHAR(255) NULL,                      -- snapshot for reports
  plant_name             VARCHAR(255) NULL,
  scrapping_date         DATE         NOT NULL,
  shift                  VARCHAR(20)  NULL,
  available_pending_qty  INT          NOT NULL,                  -- pending sleepers seen at submit
  qty_scrapped           INT          NOT NULL,                  -- sleepers witnessed
  declaration_accepted   TINYINT(1)   NOT NULL DEFAULT 0,
  declaration_text       VARCHAR(500) NOT NULL,
  remarks                TEXT         NULL,
  ie_user_id             BIGINT       NOT NULL,
  ie_name                VARCHAR(255) NULL,
  ie_type                VARCHAR(20)  NOT NULL,                  -- PROCESS_IE / MAIN_IE
  status                 VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE', -- ACTIVE / DELETED (soft delete)
  created_by             VARCHAR(50)  NULL,
  created_date           DATETIME(6)  NOT NULL,
  updated_by             VARCHAR(50)  NULL,
  updated_date           DATETIME(6)  NULL,
  UNIQUE KEY uq_ssv_verification_no (verification_no),
  KEY idx_ssv_plant_status (plant_id, status),
  KEY idx_ssv_scrapping_date (scrapping_date),
  CONSTRAINT chk_ssv_qty_positive CHECK (qty_scrapped > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. The individual sleepers destroyed in each verification.
--    active_key = plant|batch|sleeper while the item is active and NULL once released (item removed on edit or
--    verification deleted), so the UNIQUE key stops any sleeper from being scrapped twice.
CREATE TABLE sleeper_scrap_verification_item (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  scrap_verification_id  BIGINT       NOT NULL,
  plant_id               VARCHAR(100) NOT NULL,
  batch_no               VARCHAR(100) NULL,
  sleeper_no             VARCHAR(100) NOT NULL,                  -- e.g. 210-A, 1AS
  sleeper_key            VARCHAR(250) NOT NULL,                  -- normalised BATCH|SLEEPER
  active_key             VARCHAR(300) NULL,
  sleeper_type           VARCHAR(100) NULL,
  stage                  VARCHAR(30)  NULL,                      -- DEMOULDING / FINAL / TESTING
  rejection_reason       VARCHAR(500) NULL,
  rejection_date         DATE         NULL,
  created_date           DATETIME(6)  NOT NULL,
  UNIQUE KEY uq_ssvi_active_key (active_key),
  KEY idx_ssvi_verification (scrap_verification_id),
  KEY idx_ssvi_plant_key (plant_id, sleeper_key),
  CONSTRAINT fk_ssvi_verification FOREIGN KEY (scrap_verification_id)
    REFERENCES sleeper_scrap_verification (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Before / after destruction photos
CREATE TABLE sleeper_scrap_verification_image (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  scrap_verification_id  BIGINT       NOT NULL,
  photo_type             VARCHAR(10)  NOT NULL,                  -- BEFORE / AFTER
  image_url              VARCHAR(500) NOT NULL,
  blob_name              VARCHAR(255) NOT NULL,
  created_by             VARCHAR(50)  NULL,
  created_date           DATETIME(6)  NOT NULL,
  KEY idx_ssvim_verification (scrap_verification_id, photo_type),
  CONSTRAINT fk_ssvim_verification FOREIGN KEY (scrap_verification_id)
    REFERENCES sleeper_scrap_verification (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Immutable audit trail of edits / deletes (allowed only within 1 hour of creation)
CREATE TABLE sleeper_scrap_verification_audit (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  scrap_verification_id  BIGINT       NOT NULL,
  action                 VARCHAR(10)  NOT NULL,                  -- EDIT / DELETE
  field_changed          VARCHAR(100) NULL,
  old_value              TEXT       
  
    NULL,
  new_value              TEXT         NULL,
  reason                 TEXT         NOT NULL,
  action_by              VARCHAR(50)  NOT NULL,
  action_at              DATETIME(6)  NOT NULL,
  KEY idx_ssva_verification (scrap_verification_id, action_at),
  CONSTRAINT fk_ssva_verification FOREIGN KEY (scrap_verification_id)
    REFERENCES sleeper_scrap_verification (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Indexes on existing tables so the live basket queries stay fast.
--    Run SHOW INDEX FROM <table> first and skip any that already exist.
CREATE INDEX idx_ddi_plant ON demoulding_inspection (plant_id);
CREATE INDEX idx_ibs_plant ON ie_batch_summary (plant_id);
