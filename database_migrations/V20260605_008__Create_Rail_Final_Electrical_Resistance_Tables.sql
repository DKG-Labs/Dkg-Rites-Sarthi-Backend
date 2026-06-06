-- Create Rail Final Electrical Resistance table (Consolidated single table with 6 samples support)
CREATE TABLE rail_final_electrical_resistance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Before Immersion Forward (S1 to S6)
    s1_before_forward VARCHAR(50),
    s2_before_forward VARCHAR(50),
    s3_before_forward VARCHAR(50),
    s4_before_forward VARCHAR(50),
    s5_before_forward VARCHAR(50),
    s6_before_forward VARCHAR(50),
    
    -- Before Immersion Reverse (S1 to S6)
    s1_before_reverse VARCHAR(50),
    s2_before_reverse VARCHAR(50),
    s3_before_reverse VARCHAR(50),
    s4_before_reverse VARCHAR(50),
    s5_before_reverse VARCHAR(50),
    s6_before_reverse VARCHAR(50),
    
    -- After Immersion Forward (S1 to S6)
    s1_after_forward VARCHAR(50),
    s2_after_forward VARCHAR(50),
    s3_after_forward VARCHAR(50),
    s4_after_forward VARCHAR(50),
    s5_after_forward VARCHAR(50),
    s6_after_forward VARCHAR(50),
    
    -- After Immersion Reverse (S1 to S6)
    s1_after_reverse VARCHAR(50),
    s2_after_reverse VARCHAR(50),
    s3_after_reverse VARCHAR(50),
    s4_after_reverse VARCHAR(50),
    s5_after_reverse VARCHAR(50),
    s6_after_reverse VARCHAR(50),
    
    electrical_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    -- Audit fields
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_electrical_resistance (call_no, lot_no)
);
