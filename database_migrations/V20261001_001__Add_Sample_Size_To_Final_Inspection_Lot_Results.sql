-- Migration to add sample_size column to final_inspection_lot_results for ERC Final Inspection
ALTER TABLE final_inspection_lot_results 
ADD COLUMN sample_size INT NULL AFTER heat_no;
