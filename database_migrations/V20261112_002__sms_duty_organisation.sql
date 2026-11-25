-- SMS Shift Summary: carry forward incomplete heats only within the same organisation (JSPL / BSP) + SMS.
-- Run manually on each DB (local / QA / UAT / PROD).
ALTER TABLE sms_duty ADD COLUMN organisation VARCHAR(20) NULL AFTER sms;
CREATE INDEX idx_sms_duty_org_sms ON sms_duty (organisation, sms);

-- Optional backfill: JSPL has only SMS 2; old duties can't be told apart reliably, so they stay NULL
-- (their unfinished heats will show only in their own duty). Fill manually if you know the plant, e.g.:
-- UPDATE sms_duty SET organisation = 'BSP' WHERE organisation IS NULL AND sms = 'SMS 3' AND duty_id <> '';
