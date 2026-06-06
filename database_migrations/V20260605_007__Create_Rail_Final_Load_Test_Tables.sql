-- Create Rail Final Load Test table (Consolidated single table with 4 pads, each having 8 load steps)
CREATE TABLE rail_final_load_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Pad 1 L & R Gauges
    pad1_l1 VARCHAR(50), pad1_l2 VARCHAR(50), pad1_l3 VARCHAR(50), pad1_l4 VARCHAR(50),
    pad1_l5 VARCHAR(50), pad1_l6 VARCHAR(50), pad1_l7 VARCHAR(50), pad1_l8 VARCHAR(50),
    pad1_r1 VARCHAR(50), pad1_r2 VARCHAR(50), pad1_r3 VARCHAR(50), pad1_r4 VARCHAR(50),
    pad1_r5 VARCHAR(50), pad1_r6 VARCHAR(50), pad1_r7 VARCHAR(50), pad1_r8 VARCHAR(50),
    
    -- Pad 2 L & R Gauges
    pad2_l1 VARCHAR(50), pad2_l2 VARCHAR(50), pad2_l3 VARCHAR(50), pad2_l4 VARCHAR(50),
    pad2_l5 VARCHAR(50), pad2_l6 VARCHAR(50), pad2_l7 VARCHAR(50), pad2_l8 VARCHAR(50),
    pad2_r1 VARCHAR(50), pad2_r2 VARCHAR(50), pad2_r3 VARCHAR(50), pad2_r4 VARCHAR(50),
    pad2_r5 VARCHAR(50), pad2_r6 VARCHAR(50), pad2_r7 VARCHAR(50), pad2_r8 VARCHAR(50),
    
    -- Pad 3 L & R Gauges
    pad3_l1 VARCHAR(50), pad3_l2 VARCHAR(50), pad3_l3 VARCHAR(50), pad3_l4 VARCHAR(50),
    pad3_l5 VARCHAR(50), pad3_l6 VARCHAR(50), pad3_l7 VARCHAR(50), pad3_l8 VARCHAR(50),
    pad3_r1 VARCHAR(50), pad3_r2 VARCHAR(50), pad3_r3 VARCHAR(50), pad3_r4 VARCHAR(50),
    pad3_r5 VARCHAR(50), pad3_r6 VARCHAR(50), pad3_r7 VARCHAR(50), pad3_r8 VARCHAR(50),
    
    -- Pad 4 L & R Gauges
    pad4_l1 VARCHAR(50), pad4_l2 VARCHAR(50), pad4_l3 VARCHAR(50), pad4_l4 VARCHAR(50),
    pad4_l5 VARCHAR(50), pad4_l6 VARCHAR(50), pad4_l7 VARCHAR(50), pad4_l8 VARCHAR(50),
    pad4_r1 VARCHAR(50), pad4_r2 VARCHAR(50), pad4_r3 VARCHAR(50), pad4_r4 VARCHAR(50),
    pad4_r5 VARCHAR(50), pad4_r6 VARCHAR(50), pad4_r7 VARCHAR(50), pad4_r8 VARCHAR(50),
    
    load_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    -- Audit fields
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_load_test (call_no, lot_no)
);
