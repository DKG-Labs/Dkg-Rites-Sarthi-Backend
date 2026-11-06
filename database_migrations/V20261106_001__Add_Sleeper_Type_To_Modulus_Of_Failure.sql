-- MF samples: keep the Drawing No. (sleeper type) and the production declaration the sample came from.
-- Batch numbers are not unique (e.g. two "54" declarations on one casting date with different drawings),
-- so the drawing cannot be looked up again from the batch no. alone.
ALTER TABLE modulus_of_failure
    ADD COLUMN sleeper_type   VARCHAR(255) NULL,
    ADD COLUMN declaration_id BIGINT       NULL;
