-- =====================================================================
-- TABLE: sarthi_authenticator_verifications
-- PURPOSE: Store SARTHI Authenticator QR sessions, GPS coordinates, and live selfies
-- =====================================================================

CREATE TABLE IF NOT EXISTS `sarthi_authenticator_verifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` VARCHAR(100) NOT NULL UNIQUE,
    `user_id` VARCHAR(100) NOT NULL,
    `user_name` VARCHAR(150) NULL,
    `login_transaction_id` VARCHAR(100) NULL,
    `latitude` DECIMAL(10, 7) NULL,
    `longitude` DECIMAL(10, 7) NULL,
    `accuracy_meters` DOUBLE NULL,
    `location_address` TEXT NULL,
    `selfie_image_url` LONGTEXT NULL,
    `device_info` VARCHAR(255) NULL,
    `ip_address` VARCHAR(50) NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `verified_at` DATETIME NULL,
    `expires_at` DATETIME NOT NULL,
    INDEX `idx_auth_session` (`session_id`),
    INDEX `idx_auth_user` (`user_id`),
    INDEX `idx_auth_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Query to view recent authenticator logs:
-- SELECT id, session_id, user_id, user_name, latitude, longitude, accuracy_meters, status, verified_at, created_at 
-- FROM sarthi_authenticator_verifications 
-- ORDER BY created_at DESC;
