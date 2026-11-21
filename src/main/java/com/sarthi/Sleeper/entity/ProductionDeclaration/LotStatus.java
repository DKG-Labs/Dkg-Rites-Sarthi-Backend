package com.sarthi.Sleeper.entity.ProductionDeclaration;

/**
 * Lifecycle of a production batch (lot) with respect to lot-level acceptance tests.
 * Stored by name in production_declaration.lot_status.
 */
public enum LotStatus {
    /** Normal batch: flows through inspection as usual. */
    ACTIVE,
    /** Whole lot rejected (e.g. water cube strength test failed) and returned to the vendor; every sleeper is rejected. */
    REJECTED
}
