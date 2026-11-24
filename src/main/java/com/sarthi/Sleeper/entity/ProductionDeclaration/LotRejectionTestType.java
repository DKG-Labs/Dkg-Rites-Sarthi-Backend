package com.sarthi.Sleeper.entity.ProductionDeclaration;

/**
 * The kind of test that rejected a lot, so production_declaration.lot_rejected_by_test_id can be read unambiguously.
 * Stored by name in production_declaration.lot_rejected_by_test_type.
 */
public enum LotRejectionTestType {
    /** water_cube_strength_test.id */
    WATER_CUBE,
    /** moment_of_resistance_test.id (MR failed in double testing) */
    MR
}
