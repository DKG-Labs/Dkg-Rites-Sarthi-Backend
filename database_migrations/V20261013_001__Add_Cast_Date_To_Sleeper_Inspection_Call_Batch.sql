-- Migration: Add cast_date column to sleeper_inspection_call_batch
-- This column was previously @Transient (not persisted), causing date-of-casting
-- to always be NULL when reading back from DB.

ALTER TABLE sleeper_inspection_call_batch
    ADD COLUMN IF NOT EXISTS cast_date VARCHAR(20) NULL COMMENT 'Date of casting in dd/MM/yyyy format (from production_declaration)';

ALTER TABLE sleeper_inspection_call_batch
    ADD COLUMN IF NOT EXISTS previously_offered INT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS total_casted INT NULL DEFAULT 0;
