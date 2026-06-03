-- V20260602_002__Create_Rail_Raw_Material_Weighment_Tables.sql
-- Create table for storing raw material weighments and sub-items

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
