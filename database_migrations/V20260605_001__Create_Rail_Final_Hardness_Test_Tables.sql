-- Create Rail Final Hardness Test table (Consolidated single table with 10 samples support)
CREATE TABLE rail_final_hardness_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Compound A samples (1 to 10)
    sample_a1 VARCHAR(255),
    sample_a2 VARCHAR(255),
    sample_a3 VARCHAR(255),
    sample_a4 VARCHAR(255),
    sample_a5 VARCHAR(255),
    sample_a6 VARCHAR(255),
    sample_a7 VARCHAR(255),
    sample_a8 VARCHAR(255),
    sample_a9 VARCHAR(255),
    sample_a10 VARCHAR(255),
    
    -- Compound B samples (1 to 10, if CGRSP)
    sample_b1 VARCHAR(255),
    sample_b2 VARCHAR(255),
    sample_b3 VARCHAR(255),
    sample_b4 VARCHAR(255),
    sample_b5 VARCHAR(255),
    sample_b6 VARCHAR(255),
    sample_b7 VARCHAR(255),
    sample_b8 VARCHAR(255),
    sample_b9 VARCHAR(255),
    sample_b10 VARCHAR(255),
    
    hardness_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    -- Audit fields
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_hardness (call_no, lot_no)
);
