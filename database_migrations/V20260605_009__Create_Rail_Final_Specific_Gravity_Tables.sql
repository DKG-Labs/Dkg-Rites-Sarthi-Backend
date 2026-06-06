-- Create Rail Final Specific Gravity table (Consolidated single table with 6 samples for Compound A & B)
CREATE TABLE rail_final_specific_gravity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Compound A - Air Weight (S1 to S6)
    s1_a_air VARCHAR(50),
    s2_a_air VARCHAR(50),
    s3_a_air VARCHAR(50),
    s4_a_air VARCHAR(50),
    s5_a_air VARCHAR(50),
    s6_a_air VARCHAR(50),
    
    -- Compound A - Water Weight (S1 to S6)
    s1_a_water VARCHAR(50),
    s2_a_water VARCHAR(50),
    s3_a_water VARCHAR(50),
    s4_a_water VARCHAR(50),
    s5_a_water VARCHAR(50),
    s6_a_water VARCHAR(50),
    
    -- Compound B - Air Weight (S1 to S6)
    s1_b_air VARCHAR(50),
    s2_b_air VARCHAR(50),
    s3_b_air VARCHAR(50),
    s4_b_air VARCHAR(50),
    s5_b_air VARCHAR(50),
    s6_b_air VARCHAR(50),
    
    -- Compound B - Water Weight (S1 to S6)
    s1_b_water VARCHAR(50),
    s2_b_water VARCHAR(50),
    s3_b_water VARCHAR(50),
    s4_b_water VARCHAR(50),
    s5_b_water VARCHAR(50),
    s6_b_water VARCHAR(50),
    
    sg_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    -- Audit fields
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_specific_gravity (call_no, lot_no)
);
