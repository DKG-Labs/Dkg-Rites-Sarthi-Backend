-- Water Cube Testing: if a lot fails, the entire batch is rejected, returned to the vendor and all its sleepers
-- are shown as Rejected. The rejection is stored on the production batch so every screen reads one source.
-- lot_status values mirror com.sarthi.Sleeper.entity.ProductionDeclaration.LotStatus (stored as the enum name).
ALTER TABLE production_declaration
    ADD COLUMN lot_status              VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN lot_rejection_reason    VARCHAR(500) NULL,
    ADD COLUMN lot_rejected_at         DATETIME     NULL,
    ADD COLUMN lot_rejected_by         BIGINT       NULL,
    ADD COLUMN lot_rejected_by_test_id BIGINT       NULL;

CREATE INDEX idx_production_declaration_lot_status ON production_declaration (lot_status);

-- Backfill: batches whose latest water cube test (linked through its sample declaration) already failed.
-- The reason text should match sleeper.water-cube.rejection-reason in application.properties.
UPDATE production_declaration pd
JOIN (
    SELECT s.production_declaration_id AS pd_id, MAX(w.id) AS test_id
    FROM water_cube_strength_test w
    JOIN water_cube_sample_declaration s ON s.id = w.water_cube_sample_declaration_id
    WHERE s.production_declaration_id IS NOT NULL
    GROUP BY s.production_declaration_id
) latest ON latest.pd_id = pd.id
JOIN water_cube_strength_test lt ON lt.id = latest.test_id
SET pd.lot_status              = 'REJECTED',
    pd.lot_rejection_reason    = 'Water cube strength test failed',
    pd.lot_rejected_at         = COALESCE(lt.updated_date, lt.created_date, NOW()),
    pd.lot_rejected_by         = COALESCE(lt.updated_by, lt.created_by),
    pd.lot_rejected_by_test_id = lt.id
WHERE UPPER(TRIM(lt.final_test_result)) LIKE 'FAIL%';

-- Review after running: backfilled rejected batches that were already offered in an inspection call.
-- SELECT pd.id, pd.batch_number, pd.plant_id, c.call_no, c.status
-- FROM production_declaration pd
-- JOIN sleeper_inspection_call_batch b ON UPPER(TRIM(b.batch_no)) = UPPER(TRIM(pd.batch_number))
-- JOIN sleeper_inspection_call c ON c.id = b.inspection_call_id
--   AND REPLACE(COALESCE(c.plant_id, ''), ':', '') = REPLACE(COALESCE(pd.plant_id, ''), ':', '')
-- WHERE pd.lot_status = 'REJECTED';
