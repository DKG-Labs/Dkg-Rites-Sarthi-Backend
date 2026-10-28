-- Add pad evaluation columns (pad1/pad2 result, d1, d2, difference) to rail_final_load_test
ALTER TABLE rail_final_load_test
    ADD COLUMN pad1_result VARCHAR(50),
    ADD COLUMN pad2_result VARCHAR(50),
    ADD COLUMN pad1_d1 VARCHAR(50),
    ADD COLUMN pad1_d2 VARCHAR(50),
    ADD COLUMN pad2_d1 VARCHAR(50),
    ADD COLUMN pad2_d2 VARCHAR(50),
    ADD COLUMN pad1_difference VARCHAR(50),
    ADD COLUMN pad2_difference VARCHAR(50);

