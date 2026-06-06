-- ============================================================================
-- V20260606_002: Create Remaining Specialized Final Inspection Tables and Add Shift Date
-- ============================================================================

-- 1. Alter existing tables to add date_of_shift column
ALTER TABLE rail_final_visual_dimensional_inspection ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_weight_test ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_hardness_test ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_tensile_strength ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_elongation ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_modulus ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_compression_set ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_tension_set ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_load_test ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_electrical_resistance ADD COLUMN date_of_shift DATE;
ALTER TABLE rail_final_specific_gravity ADD COLUMN date_of_shift DATE;

-- 2. Create rail_final_ash_content
CREATE TABLE rail_final_ash_content (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    date_of_shift DATE,

    -- Compound A samples (3 actual + 6 marginal)
    s1_a_crucible VARCHAR(50), s1_a_sample VARCHAR(50), s1_a_ash VARCHAR(50),
    s2_a_crucible VARCHAR(50), s2_a_sample VARCHAR(50), s2_a_ash VARCHAR(50),
    s3_a_crucible VARCHAR(50), s3_a_sample VARCHAR(50), s3_a_ash VARCHAR(50),
    m1_a_crucible VARCHAR(50), m1_a_sample VARCHAR(50), m1_a_ash VARCHAR(50),
    m2_a_crucible VARCHAR(50), m2_a_sample VARCHAR(50), m2_a_ash VARCHAR(50),
    m3_a_crucible VARCHAR(50), m3_a_sample VARCHAR(50), m3_a_ash VARCHAR(50),
    m4_a_crucible VARCHAR(50), m4_a_sample VARCHAR(50), m4_a_ash VARCHAR(50),
    m5_a_crucible VARCHAR(50), m5_a_sample VARCHAR(50), m5_a_ash VARCHAR(50),
    m6_a_crucible VARCHAR(50), m6_a_sample VARCHAR(50), m6_a_ash VARCHAR(50),

    -- Compound B samples (3 actual + 6 marginal)
    s1_b_crucible VARCHAR(50), s1_b_sample VARCHAR(50), s1_b_ash VARCHAR(50),
    s2_b_crucible VARCHAR(50), s2_b_sample VARCHAR(50), s2_b_ash VARCHAR(50),
    s3_b_crucible VARCHAR(50), s3_b_sample VARCHAR(50), s3_b_ash VARCHAR(50),
    m1_b_crucible VARCHAR(50), m1_b_sample VARCHAR(50), m1_b_ash VARCHAR(50),
    m2_b_crucible VARCHAR(50), m2_b_sample VARCHAR(50), m2_b_ash VARCHAR(50),
    m3_b_crucible VARCHAR(50), m3_b_sample VARCHAR(50), m3_b_ash VARCHAR(50),
    m4_b_crucible VARCHAR(50), m4_b_sample VARCHAR(50), m4_b_ash VARCHAR(50),
    m5_b_crucible VARCHAR(50), m5_b_sample VARCHAR(50), m5_b_ash VARCHAR(50),
    m6_b_crucible VARCHAR(50), m6_b_sample VARCHAR(50), m6_b_ash VARCHAR(50),

    ash_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,

    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,

    UNIQUE KEY uk_rail_final_ash (call_no, lot_no)
);

-- 3. Create rail_final_adhesion_test
CREATE TABLE rail_final_adhesion_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    date_of_shift DATE,

    -- 2 actual + 4 marginal samples
    sample1 VARCHAR(255), sample2 VARCHAR(255),
    marginal1 VARCHAR(255), marginal2 VARCHAR(255), marginal3 VARCHAR(255), marginal4 VARCHAR(255),

    adhesion_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,

    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,

    UNIQUE KEY uk_rail_final_adhesion (call_no, lot_no)
);

-- 4. Create rail_final_secant_stiffness_test
CREATE TABLE rail_final_secant_stiffness_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    date_of_shift DATE,

    -- Sample 1 (Loads 20 & 90 with A, B, C, D deflections)
    s1_s20_a VARCHAR(50), s1_s20_b VARCHAR(50), s1_s20_c VARCHAR(50), s1_s20_d VARCHAR(50),
    s1_s90_a VARCHAR(50), s1_s90_b VARCHAR(50), s1_s90_c VARCHAR(50), s1_s90_d VARCHAR(50),

    -- Sample 2
    s2_s20_a VARCHAR(50), s2_s20_b VARCHAR(50), s2_s20_c VARCHAR(50), s2_s20_d VARCHAR(50),
    s2_s90_a VARCHAR(50), s2_s90_b VARCHAR(50), s2_s90_c VARCHAR(50), s2_s90_d VARCHAR(50),

    -- Marginal 1
    m1_s20_a VARCHAR(50), m1_s20_b VARCHAR(50), m1_s20_c VARCHAR(50), m1_s20_d VARCHAR(50),
    m1_s90_a VARCHAR(50), m1_s90_b VARCHAR(50), m1_s90_c VARCHAR(50), m1_s90_d VARCHAR(50),

    -- Marginal 2
    m2_s20_a VARCHAR(50), m2_s20_b VARCHAR(50), m2_s20_c VARCHAR(50), m2_s20_d VARCHAR(50),
    m2_s90_a VARCHAR(50), m2_s90_b VARCHAR(50), m2_s90_c VARCHAR(50), m2_s90_d VARCHAR(50),

    -- Marginal 3
    m3_s20_a VARCHAR(50), m3_s20_b VARCHAR(50), m3_s20_c VARCHAR(50), m3_s20_d VARCHAR(50),
    m3_s90_a VARCHAR(50), m3_s90_b VARCHAR(50), m3_s90_c VARCHAR(50), m3_s90_d VARCHAR(50),

    -- Marginal 4
    m4_s20_a VARCHAR(50), m4_s20_b VARCHAR(50), m4_s20_c VARCHAR(50), m4_s20_d VARCHAR(50),
    m4_s90_a VARCHAR(50), m4_s90_b VARCHAR(50), m4_s90_c VARCHAR(50), m4_s90_d VARCHAR(50),

    secant_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,

    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,

    UNIQUE KEY uk_rail_final_secant (call_no, lot_no)
);

-- 5. Create rail_final_ncr_adhesion_test
CREATE TABLE rail_final_ncr_adhesion_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    date_of_shift DATE,

    -- Samples (peel, hpull)
    s1_peel VARCHAR(50), s1_hpull VARCHAR(50),
    s2_peel VARCHAR(50), s2_hpull VARCHAR(50),
    m1_peel VARCHAR(50), m1_hpull VARCHAR(50),
    m2_peel VARCHAR(50), m2_hpull VARCHAR(50),
    m3_peel VARCHAR(50), m3_hpull VARCHAR(50),
    m4_peel VARCHAR(50), m4_hpull VARCHAR(50),

    ncr_adhesion_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,

    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,

    UNIQUE KEY uk_rail_final_ncr_adhesion (call_no, lot_no)
);

-- 6. Create rail_final_ncr_breaking_load_test
CREATE TABLE rail_final_ncr_breaking_load_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    date_of_shift DATE,

    -- 5 actual + 10 marginal
    sample1 VARCHAR(255), sample2 VARCHAR(255), sample3 VARCHAR(255), sample4 VARCHAR(255), sample5 VARCHAR(255),
    marginal1 VARCHAR(255), marginal2 VARCHAR(255), marginal3 VARCHAR(255), marginal4 VARCHAR(255), marginal5 VARCHAR(255),
    marginal6 VARCHAR(255), marginal7 VARCHAR(255), marginal8 VARCHAR(255), marginal9 VARCHAR(255), marginal10 VARCHAR(255),

    ncr_breaking_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,

    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,

    UNIQUE KEY uk_rail_final_ncr_breaking (call_no, lot_no)
);

-- 7. Create rail_final_ncr_nylon_cord_test
CREATE TABLE rail_final_ncr_nylon_cord_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    date_of_shift DATE,

    -- 3 actual + 6 marginal (denier, epi, thickness, loadAtBreak, elongation, twists)
    s1_denier VARCHAR(50), s1_epi VARCHAR(50), s1_thickness VARCHAR(50), s1_load_at_break VARCHAR(50), s1_elongation VARCHAR(50), s1_twists VARCHAR(50),
    s2_denier VARCHAR(50), s2_epi VARCHAR(50), s2_thickness VARCHAR(50), s2_load_at_break VARCHAR(50), s2_elongation VARCHAR(50), s2_twists VARCHAR(50),
    s3_denier VARCHAR(50), s3_epi VARCHAR(50), s3_thickness VARCHAR(50), s3_load_at_break VARCHAR(50), s3_elongation VARCHAR(50), s3_twists VARCHAR(50),

    m1_denier VARCHAR(50), m1_epi VARCHAR(50), m1_thickness VARCHAR(50), m1_load_at_break VARCHAR(50), m1_elongation VARCHAR(50), m1_twists VARCHAR(50),
    m2_denier VARCHAR(50), m2_epi VARCHAR(50), m2_thickness VARCHAR(50), m2_load_at_break VARCHAR(50), m2_elongation VARCHAR(50), m2_twists VARCHAR(50),
    m3_denier VARCHAR(50), m3_epi VARCHAR(50), m3_thickness VARCHAR(50), m3_load_at_break VARCHAR(50), m3_elongation VARCHAR(50), m3_twists VARCHAR(50),
    m4_denier VARCHAR(50), m4_epi VARCHAR(50), m4_thickness VARCHAR(50), m4_load_at_break VARCHAR(50), m4_elongation VARCHAR(50), m4_twists VARCHAR(50),
    m5_denier VARCHAR(50), m5_epi VARCHAR(50), m5_thickness VARCHAR(50), m5_load_at_break VARCHAR(50), m5_elongation VARCHAR(50), m5_twists VARCHAR(50),
    m6_denier VARCHAR(50), m6_epi VARCHAR(50), m6_thickness VARCHAR(50), m6_load_at_break VARCHAR(50), m6_elongation VARCHAR(50), m6_twists VARCHAR(50),

    ncr_cord_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,

    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,

    UNIQUE KEY uk_rail_final_ncr_cord (call_no, lot_no)
);
