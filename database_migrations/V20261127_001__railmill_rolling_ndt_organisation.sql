-- Rail mill: Rolling and NDT duties store the Organisation (BSP / JSPL) next to the Mill.
-- Run BEFORE deploying the backend (the duty entities read this column).
-- Existing duties keep NULL (they were started before the field existed).

ALTER TABLE rolling_duty ADD COLUMN organisation VARCHAR(20) NULL AFTER mill;
ALTER TABLE ndt_duty     ADD COLUMN organisation VARCHAR(20) NULL AFTER mill;

-- check
SELECT table_name, column_name FROM information_schema.columns
WHERE table_schema = DATABASE()
  AND table_name IN ('rolling_duty', 'ndt_duty')
  AND column_name = 'organisation';
