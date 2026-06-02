-- SQL Migration: Create Vendor Calibration Parent-Child Tables

CREATE TABLE IF NOT EXISTS `vendor_calibration_header` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `vendor_code` VARCHAR(50) NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `certificate_file_path` LONGTEXT DEFAULT NULL,
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_date` DATETIME NOT NULL,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_date` DATETIME DEFAULT NULL,
    UNIQUE KEY `uk_vendor_category` (`vendor_code`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `vendor_calibration_details` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `header_id` BIGINT NOT NULL,
    `instrument_name` VARCHAR(150) NOT NULL,
    `capacity` VARCHAR(100) DEFAULT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `used_for` VARCHAR(255) DEFAULT NULL,
    `serial_number` VARCHAR(100) DEFAULT NULL,
    `calibration_certificate_no` VARCHAR(100) DEFAULT NULL,
    `calibration_date` DATE DEFAULT NULL,
    `calibration_due_date` DATE DEFAULT NULL,
    `certifying_lab_name` VARCHAR(150) DEFAULT NULL,
    `accreditation_agency` VARCHAR(50) DEFAULT NULL,
    `notification_days` INT DEFAULT 30,
    `calibration_status` VARCHAR(50) DEFAULT 'Valid',
    `created_by` VARCHAR(50) DEFAULT NULL,
    `created_date` DATETIME NOT NULL,
    `updated_by` VARCHAR(50) DEFAULT NULL,
    `updated_date` DATETIME DEFAULT NULL,
    CONSTRAINT `fk_vendor_calibration_details_header` FOREIGN KEY (`header_id`) REFERENCES `vendor_calibration_header` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
