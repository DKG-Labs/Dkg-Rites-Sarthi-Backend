-- Create Rail Final Weight Test parent table
CREATE TABLE rail_final_weight_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    n1 INT,
    ac1 INT,
    re1 INT,
    n2 INT,
    ac2 INT,
    re2 INT,
    min_weight DOUBLE,
    max_weight DOUBLE,
    is_second_active BOOLEAN,
    
    weight_status VARCHAR(50),
    not_ok1 INT,
    not_ok2 INT,
    total_not_ok INT,
    remarks TEXT,
    
    -- Audit fields
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_weight (call_no, lot_no)
);

-- Create Rail Final Weight Test Sample child table
CREATE TABLE rail_final_weight_test_sample (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rail_final_weight_test_id BIGINT NOT NULL,
    sampling_no INT NOT NULL,
    sample_no INT NOT NULL,
    sample_value DOUBLE NOT NULL,
    is_rejected TINYINT(1) NOT NULL,
    created_date DATETIME NOT NULL,
    
    UNIQUE KEY uk_rfwts_unique (rail_final_weight_test_id, sampling_no, sample_no),
    CONSTRAINT fk_rfwts_parent FOREIGN KEY (rail_final_weight_test_id) REFERENCES rail_final_weight_test (id) ON DELETE CASCADE
);
