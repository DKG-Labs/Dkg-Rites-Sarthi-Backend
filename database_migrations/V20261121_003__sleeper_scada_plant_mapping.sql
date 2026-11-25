-- Sleeper SCADA: plant -> gateway codes used in MQTT topics  SPLR/<vendor_short>/<plant_short>/<line_id>/<STAGE>
-- (e.g. SPLR/MBSL/MBKRSA/U1/BATCHING). One row per plant; line_ids is a comma list (U1,U2).
-- The SCADA monitor dropdowns and the Topic Setup panel read this table.
-- Run once. Seed = the units the monitor had built in + MBSL / MBKRSA / U1.

CREATE TABLE IF NOT EXISTS sleeper_scada_plant_mapping (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  vendor_short_name VARCHAR(20)  NOT NULL,          -- e.g. PRIL, TIPL, MBSL   (topic part 2)
  vendor_name       VARCHAR(200) NULL,              -- e.g. Patil Rail Infrastructure Pvt. Ltd.
  plant_short_name  VARCHAR(20)  NOT NULL,          -- e.g. WDM, MBKRSA        (topic part 3)
  plant_name        VARCHAR(200) NULL,              -- e.g. Wadiyaram Unit
  plant_id          VARCHAR(100) NULL,              -- SARTHI plant_id (vendor_plant), if linked
  line_ids          VARCHAR(100) NOT NULL DEFAULT 'U1',  -- e.g. U1,U2           (topic part 4)
  active            TINYINT(1)   NOT NULL DEFAULT 1,
  created_by        VARCHAR(50)  NULL,
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by        VARCHAR(50)  NULL,
  updated_at        DATETIME     NULL,
  UNIQUE KEY uk_sleeper_scada_vendor_plant (vendor_short_name, plant_short_name)
);

INSERT IGNORE INTO sleeper_scada_plant_mapping (vendor_short_name, vendor_name, plant_short_name, plant_name, line_ids) VALUES
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'WDM', 'Wadiyaram Unit', 'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'TMQ', 'Thirumangalam',  'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'PAT', 'Pahtri',         'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'HUB', 'Hubli',          'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'SHO', 'Sholaka',        'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'ANR', 'Anara',          'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'MRZ', 'Mirza',          'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'GYA', 'Gaya',           'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'KPR', 'Kaipadar',       'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'KAR', 'Kargi',          'U1,U2'),
 ('PRIL', 'Patil Rail Infrastructure Pvt. Ltd.', 'BUW', 'ACPL',           'U1,U2'),
 ('TIPL', 'Track Innovations (India) Pvt. Ltd.', 'CDG', 'Chandigarh',     'U1,U2'),
 ('MBSL', 'MBSL',                                'MBKRSA', 'MBKRSA',      'U1');

-- Check
SELECT vendor_short_name, plant_short_name, plant_name, line_ids FROM sleeper_scada_plant_mapping ORDER BY vendor_short_name, plant_short_name;
