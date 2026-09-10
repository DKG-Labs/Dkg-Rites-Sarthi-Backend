-- Migration to support partial batch acceptance and traceability in rail process inspection
ALTER TABLE rail_process_inspection_batch 
ADD COLUMN qty_available INT NULL AFTER qty_manufactured,
ADD COLUMN qty_remaining INT NULL AFTER qty_accepted;
