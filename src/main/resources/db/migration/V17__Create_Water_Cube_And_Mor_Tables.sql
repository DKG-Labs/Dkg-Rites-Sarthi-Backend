-- =====================================================
-- Migration V17: Create Water Cube Sample Tables
-- =====================================================
-- Water Cube Strength Testing for Final Inspection
-- =====================================================

CREATE TABLE IF NOT EXISTS water_cube_sample_declaration (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    production_declaration_id BIGINT,
    batch_number             VARCHAR(100),
    casting_date             DATE,
    shift                    VARCHAR(50),
    line_no                  VARCHAR(50),
    concrete_grade           VARCHAR(50),
    created_by               BIGINT,
    created_date             DATETIME,
    updated_by               BIGINT,
    updated_date             DATETIME,

    INDEX idx_wc_decl_batch (batch_number),
    INDEX idx_wc_decl_created_by (created_by),
    INDEX idx_wc_decl_production_id (production_declaration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS water_cube_sample_detail (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    declaration_id    BIGINT NOT NULL,
    sample_number     INT,
    cube_number       INT,
    bench_number      VARCHAR(50),
    sequence          VARCHAR(50),

    INDEX idx_wc_detail_decl_id (declaration_id),
    CONSTRAINT fk_wc_detail_decl FOREIGN KEY (declaration_id)
        REFERENCES water_cube_sample_declaration(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Migration V17: Create MOR Sample Declaration Table
-- =====================================================

CREATE TABLE IF NOT EXISTS mor_sample_declaration (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    sampling_date               DATE,
    concrete_grade              VARCHAR(50),
    plant_type                  VARCHAR(100),
    shed_line                   VARCHAR(100),
    sample_identification_number VARCHAR(100),
    created_by                  BIGINT,
    created_date                DATETIME,
    updated_by                  BIGINT,
    updated_date                DATETIME,

    INDEX idx_mor_decl_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Migration V17: Create MOR Test Result Table
-- =====================================================

CREATE TABLE IF NOT EXISTS mor_test_result (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    mor_sample_id   BIGINT,
    testing_date    DATE,
    weight          DOUBLE,
    load_kn         DOUBLE,
    strength        DOUBLE,
    result          VARCHAR(10),
    remarks         TEXT,
    created_by      BIGINT,
    created_date    DATETIME,
    updated_by      BIGINT,
    updated_date    DATETIME,

    INDEX idx_mor_result_sample_id (mor_sample_id),
    CONSTRAINT fk_mor_result_sample FOREIGN KEY (mor_sample_id)
        REFERENCES mor_sample_declaration(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
