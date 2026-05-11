-- Production Declaration Tables (Nested Structure)

-- 1. Declaration Header Table (Already exists but updated to remove legacy flat fields)
CREATE TABLE IF NOT EXISTS `rail_production_declaration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `vendor_name` VARCHAR(255) DEFAULT NULL,
  `vendor_code` VARCHAR(255) DEFAULT NULL,
  `plant_id` VARCHAR(255) DEFAULT NULL,
  `shift` VARCHAR(50) DEFAULT NULL,
  `production_date` DATE DEFAULT NULL,
  `production_line` VARCHAR(255) DEFAULT NULL,
  `status` VARCHAR(50) DEFAULT 'PENDING',
  `is_active` BIT(1) DEFAULT b'1',
  `created_by` BIGINT DEFAULT NULL,
  `created_date` DATETIME DEFAULT NULL,
  `updated_by` BIGINT DEFAULT NULL,
  `updated_date` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 2. Product-wise Block Table
CREATE TABLE IF NOT EXISTS `rail_production_product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `declaration_id` BIGINT NOT NULL,
  `product_type` VARCHAR(255) DEFAULT NULL,
  `measurement_mode` VARCHAR(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_declaration_id` (`declaration_id`),
  CONSTRAINT `FK_declaration_product` FOREIGN KEY (`declaration_id`) REFERENCES `rail_production_declaration` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Batch Details Table
CREATE TABLE IF NOT EXISTS `rail_production_batch` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT NOT NULL,
  `batch_no` VARCHAR(255) DEFAULT NULL,
  `comp_abatch` VARCHAR(255) DEFAULT NULL,
  `comp_bbatch` VARCHAR(255) DEFAULT NULL,
  `initial_wt` DOUBLE DEFAULT NULL,
  `final_wt` DOUBLE DEFAULT NULL,
  `quantity` INT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_product_id` (`product_id`),
  CONSTRAINT `FK_product_batch` FOREIGN KEY (`product_id`) REFERENCES `rail_production_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
