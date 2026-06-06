-- ============================================================================
-- V20260606_003: Create Final Inspection Section Results Child Table
-- ============================================================================

CREATE TABLE rail_final_inspection_section_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lot_result_id BIGINT NOT NULL,
    section_key VARCHAR(100) NOT NULL,
    section_name VARCHAR(255) NOT NULL,
    sample_size VARCHAR(100),
    status VARCHAR(50),
    FOREIGN KEY (lot_result_id) REFERENCES rail_final_inspection_lot_results(id) ON DELETE CASCADE,
    UNIQUE KEY uk_lot_section (lot_result_id, section_key)
);
