-- V20260603_001__Create_Rail_Hydraulic_Press_Tables.sql
-- Create table for storing Moulding at Hydraulic Press parameters

CREATE TABLE IF NOT EXISTS rail_hydraulic_press (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plant_id VARCHAR(255) NULL,
    vendor_code VARCHAR(255) NULL,
    shift VARCHAR(50) NULL,
    casting_date DATE NULL,
    rail_pad_type VARCHAR(255) NOT NULL,
    batch_no VARCHAR(100) NOT NULL,
    time_of_check VARCHAR(100) NOT NULL,
    curing_time DOUBLE NOT NULL,
    curing_temp DOUBLE NOT NULL,
    curing_pressure DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp VARCHAR(100) NOT NULL,
    created_by BIGINT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
