-- Create Rail Final Tensile Strength table (Consolidated single table with 10 samples support)
CREATE TABLE rail_final_tensile_strength (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Before Ageing samples (1 to 10)
    sample_before1 VARCHAR(255),
    sample_before2 VARCHAR(255),
    sample_before3 VARCHAR(255),
    sample_before4 VARCHAR(255),
    sample_before5 VARCHAR(255),
    sample_before6 VARCHAR(255),
    sample_before7 VARCHAR(255),
    sample_before8 VARCHAR(255),
    sample_before9 VARCHAR(255),
    sample_before10 VARCHAR(255),
    
    -- After Ageing samples (1 to 10)
    sample_after1 VARCHAR(255),
    sample_after2 VARCHAR(255),
    sample_after3 VARCHAR(255),
    sample_after4 VARCHAR(255),
    sample_after5 VARCHAR(255),
    sample_after6 VARCHAR(255),
    sample_after7 VARCHAR(255),
    sample_after8 VARCHAR(255),
    sample_after9 VARCHAR(255),
    sample_after10 VARCHAR(255),
    
    tensile_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    -- Audit fields
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_tensile (call_no, lot_no)
);
