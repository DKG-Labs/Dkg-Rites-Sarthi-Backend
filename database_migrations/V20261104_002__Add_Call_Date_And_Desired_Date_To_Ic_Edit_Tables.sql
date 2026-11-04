-- Migration: Add date_of_call and desired_date columns to IC edit and save_changes tables
-- Author: SARTHI AI
-- Date: 2026-11-04

-- 1. ERC Final IC Edit & Save Changes
ALTER TABLE final_ic_edit ADD COLUMN IF NOT EXISTS date_of_call VARCHAR(255) NULL AFTER no_of_visits;
ALTER TABLE final_ic_edit ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER date_of_call;

ALTER TABLE final_ic_save_changes ADD COLUMN IF NOT EXISTS date_of_call VARCHAR(255) NULL AFTER no_of_visits;
ALTER TABLE final_ic_save_changes ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER date_of_call;

-- 2. ERC Process IC Edit & Save Changes
ALTER TABLE process_ic_edit ADD COLUMN IF NOT EXISTS call_date VARCHAR(255) NULL AFTER man_days;
ALTER TABLE process_ic_edit ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER call_date;

ALTER TABLE process_ic_save_changes ADD COLUMN IF NOT EXISTS call_date VARCHAR(255) NULL AFTER man_days;
ALTER TABLE process_ic_save_changes ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER call_date;

-- 3. ERC RM IC Edit & Save Changes
ALTER TABLE rm_ic_edit ADD COLUMN IF NOT EXISTS call_date VARCHAR(255) NULL AFTER visits_no;
ALTER TABLE rm_ic_edit ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER call_date;

ALTER TABLE rm_ic_save_changes ADD COLUMN IF NOT EXISTS call_date VARCHAR(255) NULL AFTER visits_no;
ALTER TABLE rm_ic_save_changes ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER call_date;

-- 4. Sleeper Final IC Edit & Save Changes
ALTER TABLE sleeper_final_ic_edit ADD COLUMN IF NOT EXISTS date_of_call VARCHAR(255) NULL AFTER no_of_visits;
ALTER TABLE sleeper_final_ic_edit ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER date_of_call;

ALTER TABLE sleeper_final_ic_save_changes ADD COLUMN IF NOT EXISTS date_of_call VARCHAR(255) NULL AFTER no_of_visits;
ALTER TABLE sleeper_final_ic_save_changes ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER date_of_call;

-- 5. Railpad Final IC Edit & Save Changes (date_of_call already exists in railpad_final_ic_edit/save_changes)
ALTER TABLE railpad_final_ic_edit ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER date_of_call;
ALTER TABLE railpad_final_ic_save_changes ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER date_of_call;

-- 6. Railpad Process IC Edit & Save Changes (date_of_call already exists in railpad_process_ic_edit/save_changes)
ALTER TABLE railpad_process_ic_edit ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER date_of_call;
ALTER TABLE railpad_process_ic_save_changes ADD COLUMN IF NOT EXISTS desired_date VARCHAR(255) NULL AFTER date_of_call;

