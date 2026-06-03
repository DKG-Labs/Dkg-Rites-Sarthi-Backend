-- V20260602_003__Create_Rail_Mixing_Kneader_Mill_Tables.sql
-- Create table for storing Mixing at Kneader & Mixing Mill parameters

CREATE TABLE IF NOT EXISTS rail_mixing_kneader_mill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plant_id VARCHAR(255) NULL,
    vendor_code VARCHAR(255) NULL,
    shift VARCHAR(50) NULL,
    casting_date DATE NULL,
    rail_pad_type VARCHAR(255) NOT NULL,
    batch_no VARCHAR(100) NOT NULL,
    mixing_time DOUBLE NOT NULL,
    mixing_temp DOUBLE NOT NULL,
    water_circulation VARCHAR(50) NOT NULL,
    dust_collector VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    timestamp VARCHAR(100) NOT NULL,
    created_by BIGINT NULL,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT NULL,
    updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
