-- Create Rail Final Visual & Dimensional Inspection table
CREATE TABLE rail_final_visual_dimensional_inspection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Visual Inspection fields
    visual_samples INT,
    visual_not_ok INT,
    visual_reason VARCHAR(255),
    visual_result VARCHAR(50),
    
    -- Dimensional Inspection fields
    dimensional_samples INT,
    dimensional_not_ok INT,
    dimensional_reason VARCHAR(255),
    dimensional_result VARCHAR(50),
    
    -- Total Rejected
    total_rejected INT,
    
    -- Audit fields
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_vis_dim (call_no, lot_no)
);
