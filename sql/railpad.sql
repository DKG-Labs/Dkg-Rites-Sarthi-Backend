-- RailPad Module - Database Schema
-- Last Updated: 2026-05-14

-- 1. Plant Set Up (Parent Table)
CREATE TABLE rail_plant_setup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name VARCHAR(255),
    vendor_code VARCHAR(100),
    number_of_units INT,
    plant_id VARCHAR(100),
    shift VARCHAR(50),
    created_by BIGINT,
    updated_by BIGINT,
    created_date DATETIME,
    updated_date DATETIME
);

-- 2. Plant Units (Child Table)
CREATE TABLE rail_plant_unit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_name VARCHAR(255),
    address TEXT,
    num_lines INT,
    plant_setup_id BIGINT,
    CONSTRAINT fk_plant_setup FOREIGN KEY (plant_setup_id) REFERENCES rail_plant_setup(id) ON DELETE CASCADE
);

-- 3. Unit Products / RDSO Approvals (Sub-Child Table)
CREATE TABLE rail_unit_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255),
    approval_no VARCHAR(100),
    approval_date DATE,
    capacity INT,
    plant_unit_id BIGINT,
    CONSTRAINT fk_plant_unit FOREIGN KEY (plant_unit_id) REFERENCES rail_plant_unit(id) ON DELETE CASCADE
);

-- 4. Raw Material Source (Parent Table)
CREATE TABLE rail_raw_material_source (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name VARCHAR(255),
    vendor_code VARCHAR(100),
    plant_id VARCHAR(100),
    shift VARCHAR(50),
    material_name VARCHAR(255),
    material_type VARCHAR(255),
    supplier_name VARCHAR(255),
    doc_ref_no VARCHAR(100),
    doc_date DATE,
    created_by BIGINT,
    updated_by BIGINT,
    created_date DATETIME,
    updated_date DATETIME
);

-- 5. Product Recipe (Parent Table)
CREATE TABLE rail_product_recipe (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name VARCHAR(255),
    vendor_code VARCHAR(100),
    plant_id VARCHAR(100),
    shift VARCHAR(50),
    recipe_identification VARCHAR(255),
    pad_type VARCHAR(255),
    total_percentage DOUBLE,
    virgin_total_percentage DOUBLE,
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME
);

-- 6. Recipe Ingredients (Child Table)
CREATE TABLE rail_recipe_ingredient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT,
    raw_material VARCHAR(255),
    percentage DOUBLE,
    CONSTRAINT fk_recipe FOREIGN KEY (recipe_id) REFERENCES rail_product_recipe(id) ON DELETE CASCADE
);

-- 7. Approved Ash & SG (Parent Table)
CREATE TABLE rail_approved_ash_sg (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name VARCHAR(255),
    vendor_code VARCHAR(100),
    plant_id VARCHAR(100),
    shift VARCHAR(50),
    pad_type VARCHAR(255),
    ash_content_a DOUBLE,
    specific_gravity_a DOUBLE,
    ash_content_b DOUBLE,
    specific_gravity_b DOUBLE,
    approval_ref_no VARCHAR(255),
    approval_date DATE,
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME
);

-- 8. Approved QAP Values (Parent Table)
CREATE TABLE rail_approved_qap (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name VARCHAR(255),
    vendor_code VARCHAR(100),
    plant_id VARCHAR(100),
    shift VARCHAR(50),
    qap_no VARCHAR(100),
    approving_authority VARCHAR(255),
    approval_date DATE,
    effective_date DATE,
    validity_date DATE,
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME
);

-- 9. QAP Product Details (Child Table)
CREATE TABLE rail_qap_product_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    qap_id BIGINT,
    pad_type VARCHAR(255),
    min_mixing_time DOUBLE,
    max_mixing_time DOUBLE,
    min_mixing_temp DOUBLE,
    max_mixing_temp DOUBLE,
    mixing_weight DOUBLE,
    min_curing_time DOUBLE,
    max_curing_time DOUBLE,
    min_curing_temp DOUBLE,
    max_curing_temp DOUBLE,
    min_curing_pressure DOUBLE,
    max_curing_pressure DOUBLE,
    CONSTRAINT fk_qap FOREIGN KEY (qap_id) REFERENCES rail_approved_qap(id) ON DELETE CASCADE
);

-- 10. Rail Production Declaration (Header)
CREATE TABLE IF NOT EXISTS rail_production_declaration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name VARCHAR(255),
    vendor_code VARCHAR(255),
    plant_id VARCHAR(255),
    shift VARCHAR(50),
    production_date DATE,
    production_line VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING',
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 11. Rail Production Product (Child)
CREATE TABLE IF NOT EXISTS rail_production_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    declaration_id BIGINT NOT NULL,
    product_type VARCHAR(255),
    measurement_mode VARCHAR(50), -- Sets / Pieces
    CONSTRAINT fk_declaration_header FOREIGN KEY (declaration_id) REFERENCES rail_production_declaration(id) ON DELETE CASCADE
);

-- 12. Rail Production Batch (Sub-Child)
CREATE TABLE IF NOT EXISTS rail_production_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    batch_no VARCHAR(100),
    comp_abatch VARCHAR(100),
    comp_bbatch VARCHAR(100),
    initial_wt DOUBLE,
    final_wt DOUBLE,
    quantity INT,
    CONSTRAINT fk_product_parent FOREIGN KEY (product_id) REFERENCES rail_production_product(id) ON DELETE CASCADE
);

-- 13. Rail IE Production Verification (Parent)
CREATE TABLE IF NOT EXISTS rail_ie_production_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    casting_date DATE,
    shift VARCHAR(50),
    production_unit VARCHAR(255),
    request_id BIGINT,
    total_pieces_produced INT,
    total_pieces_rejected INT,
    total_accepted_pieces INT,
    created_by BIGINT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 14. Rail IE Production Info (Child 1 - Vendor Production Information)
CREATE TABLE IF NOT EXISTS rail_ie_production_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    verification_id BIGINT NOT NULL,
    product_type VARCHAR(255),
    batch_no VARCHAR(100),
    initial_wt DOUBLE,
    final_wt DOUBLE,
    quantity_produced INT,
    CONSTRAINT fk_ie_verification FOREIGN KEY (verification_id) REFERENCES rail_ie_production_verification(id) ON DELETE CASCADE
);

-- 15. Rail IE Production Rejection (Child 2 - Log Physical Rejections)
CREATE TABLE IF NOT EXISTS rail_ie_production_rejection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    verification_id BIGINT NOT NULL,
    info_id BIGINT,
    product_type VARCHAR(255),
    batch_no VARCHAR(100),
    rejected_qty INT,
    reason VARCHAR(255),
    CONSTRAINT fk_ie_verification_rej FOREIGN KEY (verification_id) REFERENCES rail_ie_production_verification(id) ON DELETE CASCADE,
    CONSTRAINT fk_ie_info_rej FOREIGN KEY (info_id) REFERENCES rail_ie_production_info(id) ON DELETE SET NULL
);

-- 16. Rail Inspection Call (Parent)
CREATE TABLE IF NOT EXISTS rail_inspection_call (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(50) UNIQUE,
    po_no VARCHAR(50),
    po_sr VARCHAR(20) NULL COMMENT 'PO item serial number e.g. 001, maps to po_item.item_sr_no',
    vendor_code VARCHAR(50),
    plant_id VARCHAR(50),
    rail_pad_type VARCHAR(100),
    total_qty INT,
    no_of_lots INT,
    inspection_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_by BIGINT,
    created_at DATETIME,
    updated_by BIGINT,
    updated_at DATETIME
);

-- 17. Rail Inspection Lot (Child)
CREATE TABLE IF NOT EXISTS rail_inspection_lot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_id BIGINT,
    lot_no VARCHAR(50),
    lot_size INT,
    CONSTRAINT fk_inspection_call FOREIGN KEY (call_id) REFERENCES rail_inspection_call(id) ON DELETE CASCADE
);

-- 18. Rail Inspection Batch (Grandchild)
CREATE TABLE IF NOT EXISTS rail_inspection_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lot_id BIGINT,
    batch_no VARCHAR(50),
    quantity INT,
    production_date DATE,
    CONSTRAINT fk_inspection_lot FOREIGN KEY (lot_id) REFERENCES rail_inspection_lot(id) ON DELETE CASCADE
);

-- 19. Rail Inspection Schedule
CREATE TABLE IF NOT EXISTS rail_inspection_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(50) NOT NULL UNIQUE,
    schedule_date DATE NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(50) DEFAULT 'Scheduled',
    created_by VARCHAR(100),
    created_at DATETIME,
    updated_by VARCHAR(100),
    updated_at DATETIME
);

-- 20. Rail Initiation Verification
-- Stores the IE officer's verified Section A & B data for each inspection call.
-- Also stores the shift details entered via the ShiftDutyForm modal.
CREATE TABLE IF NOT EXISTS rail_initiation_verification (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no                 VARCHAR(50) NOT NULL UNIQUE COMMENT 'Inspection call number e.g. RPF-0513002',

    -- Section A: PO Information (verified by IE)
    rly_po_no               VARCHAR(100)    COMMENT 'RLY/PO_NO e.g. SER/60260074102063',
    po_no                   VARCHAR(50),
    po_date                 VARCHAR(20),
    po_qty                  INT,
    po_sr_qty               INT,
    vendor_name             VARCHAR(255),
    vendor_code             VARCHAR(50),
    ma_no                   VARCHAR(50),
    ma_date                 VARCHAR(20),
    purchasing_authority    VARCHAR(500),
    bill_paying_officer     VARCHAR(255),
    section_a_status        VARCHAR(20)     DEFAULT 'approved',

    -- Section B: Inspection Call Details (verified by IE)
    rly_po_no_serial        VARCHAR(150)    COMMENT 'RLY/PO_NO/PO_SR e.g. SER/60260074102063/001',
    item_desc               TEXT,
    erc_type                VARCHAR(100),
    unit                    VARCHAR(50),
    consignee               VARCHAR(255),
    orig_dp                 VARCHAR(20),
    ext_dp                  VARCHAR(20),
    call_qty                VARCHAR(50),
    qty_unit                VARCHAR(50),
    place_of_inspection     VARCHAR(255),
    remarks                 TEXT,
    section_b_status        VARCHAR(20)     DEFAULT 'approved',

    -- Shift Details (from ShiftDutyForm modal on "OPEN & VERIFY FORM")
    shift                   VARCHAR(10),
    company                 VARCHAR(255),
    casting_date            DATE,
    production_unit         VARCHAR(255),

    -- Audit
    verified_by             BIGINT,
    verified_at             DATETIME        DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- MIGRATION SCRIPTS
-- Run these on existing databases (tables already created).
-- =============================================================================

-- 2026-05-14: Add po_sr to rail_inspection_call
ALTER TABLE rail_inspection_call
    ADD COLUMN IF NOT EXISTS po_sr VARCHAR(20) NULL
    COMMENT 'PO item serial number e.g. 001, maps to po_item.item_sr_no';

-- 2026-05-14: Create rail_initiation_verification (idempotent via CREATE TABLE IF NOT EXISTS above)

-- 2026-05-14: Add po_no to rail_production_declaration
ALTER TABLE rail_production_declaration
    ADD COLUMN po_no VARCHAR(100) NULL
    AFTER production_line;

-- 2026-06-02: Create Raw Material Weighment tables
CREATE TABLE IF NOT EXISTS rail_raw_material_weighment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plant_id VARCHAR(255) NULL,
    vendor_code VARCHAR(255) NULL,
    shift VARCHAR(50) NULL,
    casting_date DATE NULL,
    rail_pad_type VARCHAR(255) NOT NULL,
    batch_no VARCHAR(100) NOT NULL,
    total_weight DOUBLE NOT NULL,
    accepted_materials VARCHAR(50) NOT NULL,
    contract_specification VARCHAR(255) NOT NULL,
    rubber_percentage DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp VARCHAR(100) NOT NULL,
    created_by BIGINT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rail_raw_material_weighment_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    weighment_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    weight DOUBLE NOT NULL,
    CONSTRAINT fk_weighment FOREIGN KEY (weighment_id) REFERENCES rail_raw_material_weighment(id) ON DELETE CASCADE
);

