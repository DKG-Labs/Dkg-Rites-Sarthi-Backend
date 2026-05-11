-- RailPad Module - Database Schema
-- Last Updated: 2026-05-08

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

