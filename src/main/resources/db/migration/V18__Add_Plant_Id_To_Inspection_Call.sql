-- Migration V18: Add Plant ID to Sleeper Inspection Call table
ALTER TABLE sleeper_inspection_call ADD COLUMN plant_id VARCHAR(255);
