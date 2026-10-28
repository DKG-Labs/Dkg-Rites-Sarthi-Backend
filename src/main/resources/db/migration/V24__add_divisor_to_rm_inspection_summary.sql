-- Add divisor column to rm_inspection_summary
-- Stores the custom kg/piece divisor used for Nos calculation (user-editable, min = ERC type default)
ALTER TABLE rm_inspection_summary
    ADD COLUMN divisor DECIMAL(10, 4) NULL;
