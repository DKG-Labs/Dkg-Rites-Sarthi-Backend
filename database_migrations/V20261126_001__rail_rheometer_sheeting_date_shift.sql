-- Rail Pad IE: Rheometer test and Sheeting / Sizing store the date and shift of the check.
-- Duplicate rule changes from "same batch at the plant, ever" to "same batch + same date + same shift at the plant"
-- (plants restart batch numbers, so a recycled batch number on another day is a new check).
-- Existing rows keep NULL date / shift and never block a new entry.
-- Run BEFORE deploying the backend that reads these columns.

ALTER TABLE rail_rheometer_test
    ADD COLUMN casting_date VARCHAR(50) NULL AFTER batch_no,
    ADD COLUMN shift        VARCHAR(50) NULL AFTER casting_date;

ALTER TABLE rail_sheeting_sizing
    ADD COLUMN casting_date VARCHAR(50) NULL AFTER batch_no,
    ADD COLUMN shift        VARCHAR(50) NULL AFTER casting_date;

-- check
SELECT table_name, column_name FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('rail_rheometer_test', 'rail_sheeting_sizing')
  AND column_name IN ('casting_date', 'shift');
