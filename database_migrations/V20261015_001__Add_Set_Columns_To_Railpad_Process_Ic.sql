ALTER TABLE `railpad_process_ic_save_changes`
  ADD COLUMN `offered_sets` INT NULL,
  ADD COLUMN `accepted_sets` INT NULL,
  ADD COLUMN `rejected_sets` INT NULL;

ALTER TABLE `railpad_process_ic_edit`
  ADD COLUMN `offered_sets` INT NULL,
  ADD COLUMN `accepted_sets` INT NULL,
  ADD COLUMN `rejected_sets` INT NULL;
