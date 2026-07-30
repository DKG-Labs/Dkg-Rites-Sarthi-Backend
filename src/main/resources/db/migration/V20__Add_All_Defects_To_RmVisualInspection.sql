-- Migration to add All Defects boolean and All Defects Remark columns to rm_visual_inspection table
ALTER TABLE rm_visual_inspection ADD COLUMN IF NOT EXISTS all_defects BOOLEAN DEFAULT FALSE;
ALTER TABLE rm_visual_inspection ADD COLUMN IF NOT EXISTS all_defects_remark VARCHAR(500);
