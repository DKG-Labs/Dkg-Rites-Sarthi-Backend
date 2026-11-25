-- Plant Operational Status & Downtime module: separate tables for Sleeper and Rail Pad.
-- Run once on each environment. Photos go to Azure container 'plant-status' (folders sleeper/ and railpad/) (URLs stored in photo_urls).

-- ===== SLEEPER =====
CREATE TABLE sleeper_plant_status_reason_master (
  id INT AUTO_INCREMENT PRIMARY KEY,
  category VARCHAR(10) NOT NULL,            -- MINOR / MAJOR
  reason VARCHAR(150) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uk_sleeper_psrm (category, reason)
);
INSERT INTO sleeper_plant_status_reason_master (category, reason, sort_order) VALUES
('MINOR','Weekly off / public holiday / festival',1),('MINOR','Scheduled maintenance / routine servicing',2),
('MINOR','Shortage of manpower / staff absence',3),('MINOR','No production schedule / production order',4),
('MINOR','Temporary shortage of raw material',5),('MINOR','Temporary power / water supply issue',6),('MINOR','Other',99),
('MAJOR','Major machinery / equipment breakdown',1),('MAJOR','Electrical / utility system failure',2),
('MAJOR','Fire / accident / industrial incident',3),('MAJOR','Structural damage / infrastructure failure',4),
('MAJOR','Major equipment failure affecting production',5),('MAJOR','Regulatory / safety-related shutdown',6),('MAJOR','Other',99);

CREATE TABLE sleeper_plant_status_declaration (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  poi_code VARCHAR(50) NOT NULL,
  plant_id VARCHAR(100),
  plant_name VARCHAR(255),
  vendor_code VARCHAR(50),
  ie_user_id INT NOT NULL,
  ie_name VARCHAR(150),
  declaration_date DATE NOT NULL,           -- IST; shift C after midnight -> previous date
  shift CHAR(1) NOT NULL,                   -- A 06-14, B 14-22, C 22-06
  operational_status VARCHAR(20) NOT NULL,  -- FULLY / PARTIALLY / NON_FUNCTIONAL
  production_status VARCHAR(15) NOT NULL,   -- RUNNING / NOT_RUNNING
  reason_category VARCHAR(10),              -- MINOR / MAJOR
  reason_id INT,
  affected_facility VARCHAR(255),
  remarks VARCHAR(1000),
  expected_restoration_at DATETIME,
  photo_urls JSON,                          -- Azure blob URLs (container plant-status-photos)
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL,
  updated_by INT NULL,
  UNIQUE KEY uk_sleeper_psd (poi_code, ie_user_id, declaration_date, shift),
  KEY idx_sleeper_psd_plant_date (poi_code, declaration_date),
  KEY idx_sleeper_psd_ie_date (ie_user_id, declaration_date),
  CONSTRAINT fk_sleeper_psd_reason FOREIGN KEY (reason_id) REFERENCES sleeper_plant_status_reason_master(id)
);

CREATE TABLE sleeper_plant_status_declaration_audit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  declaration_id BIGINT NOT NULL,
  old_value JSON,
  new_value JSON,
  changed_by INT NOT NULL,
  changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_sleeper_psda_decl (declaration_id)
);

-- ===== RAIL PAD =====
CREATE TABLE rail_plant_status_reason_master (
  id INT AUTO_INCREMENT PRIMARY KEY,
  category VARCHAR(10) NOT NULL,            -- MINOR / MAJOR
  reason VARCHAR(150) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  active TINYINT(1) NOT NULL DEFAULT 1,
  UNIQUE KEY uk_rail_psrm (category, reason)
);
INSERT INTO rail_plant_status_reason_master (category, reason, sort_order) VALUES
('MINOR','Weekly off / public holiday / festival',1),('MINOR','Scheduled maintenance / routine servicing',2),
('MINOR','Shortage of manpower / staff absence',3),('MINOR','No production schedule / production order',4),
('MINOR','Temporary shortage of raw material',5),('MINOR','Temporary power / water supply issue',6),('MINOR','Other',99),
('MAJOR','Major machinery / equipment breakdown',1),('MAJOR','Electrical / utility system failure',2),
('MAJOR','Fire / accident / industrial incident',3),('MAJOR','Structural damage / infrastructure failure',4),
('MAJOR','Major equipment failure affecting production',5),('MAJOR','Regulatory / safety-related shutdown',6),('MAJOR','Other',99);

CREATE TABLE rail_plant_status_declaration (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  poi_code VARCHAR(50) NOT NULL,
  plant_id VARCHAR(100),
  plant_name VARCHAR(255),
  vendor_code VARCHAR(50),
  ie_user_id INT NOT NULL,
  ie_name VARCHAR(150),
  declaration_date DATE NOT NULL,           -- IST; shift C after midnight -> previous date
  shift CHAR(1) NOT NULL,                   -- A 06-14, B 14-22, C 22-06
  operational_status VARCHAR(20) NOT NULL,  -- FULLY / PARTIALLY / NON_FUNCTIONAL
  production_status VARCHAR(15) NOT NULL,   -- RUNNING / NOT_RUNNING
  reason_category VARCHAR(10),              -- MINOR / MAJOR
  reason_id INT,
  affected_facility VARCHAR(255),
  remarks VARCHAR(1000),
  expected_restoration_at DATETIME,
  photo_urls JSON,                          -- Azure blob URLs (container plant-status-photos)
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL,
  updated_by INT NULL,
  UNIQUE KEY uk_rail_psd (poi_code, ie_user_id, declaration_date, shift),
  KEY idx_rail_psd_plant_date (poi_code, declaration_date),
  KEY idx_rail_psd_ie_date (ie_user_id, declaration_date),
  CONSTRAINT fk_rail_psd_reason FOREIGN KEY (reason_id) REFERENCES rail_plant_status_reason_master(id)
);

CREATE TABLE rail_plant_status_declaration_audit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  declaration_id BIGINT NOT NULL,
  old_value JSON,
  new_value JSON,
  changed_by INT NOT NULL,
  changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_rail_psda_decl (declaration_id)
);
