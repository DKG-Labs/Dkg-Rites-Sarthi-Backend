-- Migration to widen process_ic_number in final_inspection_lot_details to support comma-separated multi-select IC numbers
ALTER TABLE final_inspection_lot_details MODIFY COLUMN process_ic_number TEXT;
