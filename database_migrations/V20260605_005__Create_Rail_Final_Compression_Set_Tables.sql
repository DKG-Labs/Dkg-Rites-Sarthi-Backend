-- Create Rail Final Compression Set table (Consolidated single table with 6 samples support)
CREATE TABLE rail_final_compression_set (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Initial Thickness (A) samples (1 to 6)
    sample_initial1 VARCHAR(255),
    sample_initial2 VARCHAR(255),
    sample_initial3 VARCHAR(255),
    sample_initial4 VARCHAR(255),
    sample_initial5 VARCHAR(255),
    sample_initial6 VARCHAR(255),
    
    -- Final Thickness (B) samples (1 to 6)
    sample_final1 VARCHAR(255),
    sample_final2 VARCHAR(255),
    sample_final3 VARCHAR(255),
    sample_final4 VARCHAR(255),
    sample_final5 VARCHAR(255),
    sample_final6 VARCHAR(255),
    
    compression_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    -- Audit fields
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_compression_set (call_no, lot_no)
);
