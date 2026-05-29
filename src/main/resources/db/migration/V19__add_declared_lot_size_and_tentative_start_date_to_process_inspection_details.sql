-- V19: Add declared_lot_size and tentative_start_date to process_inspection_details
-- These fields are filled by the vendor when raising a Process Inspection Call
-- and must be preserved through the modify workflow

ALTER TABLE process_inspection_details
    ADD COLUMN IF NOT EXISTS declared_lot_size INTEGER,
    ADD COLUMN IF NOT EXISTS tentative_start_date DATE;
