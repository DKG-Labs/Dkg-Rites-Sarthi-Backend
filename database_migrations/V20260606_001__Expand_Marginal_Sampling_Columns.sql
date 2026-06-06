-- ============================================================================
-- V20260606_001: Expand Marginal Double-Sampling Columns
-- Rule: marginal count = 2 × primary count
-- Column naming: sample_* for actual, marginal_* for marginal (double-sampling)
-- ============================================================================

-- ============================================================================
-- 1. HARDNESS TEST (5 actual + 10 marginal = 15 per compound)
-- ============================================================================
DROP TABLE IF EXISTS rail_final_hardness_test;
CREATE TABLE rail_final_hardness_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Compound A actual samples (1 to 5)
    sample_a1 VARCHAR(255), sample_a2 VARCHAR(255), sample_a3 VARCHAR(255),
    sample_a4 VARCHAR(255), sample_a5 VARCHAR(255),
    
    -- Compound B actual samples (1 to 5, if CGRSP)
    sample_b1 VARCHAR(255), sample_b2 VARCHAR(255), sample_b3 VARCHAR(255),
    sample_b4 VARCHAR(255), sample_b5 VARCHAR(255),
    
    -- Compound A marginal samples (1 to 10)
    marginal_a1 VARCHAR(255), marginal_a2 VARCHAR(255), marginal_a3 VARCHAR(255),
    marginal_a4 VARCHAR(255), marginal_a5 VARCHAR(255), marginal_a6 VARCHAR(255),
    marginal_a7 VARCHAR(255), marginal_a8 VARCHAR(255), marginal_a9 VARCHAR(255),
    marginal_a10 VARCHAR(255),
    
    -- Compound B marginal samples (1 to 10, if CGRSP)
    marginal_b1 VARCHAR(255), marginal_b2 VARCHAR(255), marginal_b3 VARCHAR(255),
    marginal_b4 VARCHAR(255), marginal_b5 VARCHAR(255), marginal_b6 VARCHAR(255),
    marginal_b7 VARCHAR(255), marginal_b8 VARCHAR(255), marginal_b9 VARCHAR(255),
    marginal_b10 VARCHAR(255),
    
    hardness_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_hardness (call_no, lot_no)
);

-- ============================================================================
-- 2. TENSILE STRENGTH (5 actual + 10 marginal = 15 per category)
-- ============================================================================
DROP TABLE IF EXISTS rail_final_tensile_strength;
CREATE TABLE rail_final_tensile_strength (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Before Ageing actual samples (1 to 5)
    sample_before1 VARCHAR(255), sample_before2 VARCHAR(255), sample_before3 VARCHAR(255),
    sample_before4 VARCHAR(255), sample_before5 VARCHAR(255),
    
    -- After Ageing actual samples (1 to 5)
    sample_after1 VARCHAR(255), sample_after2 VARCHAR(255), sample_after3 VARCHAR(255),
    sample_after4 VARCHAR(255), sample_after5 VARCHAR(255),
    
    -- Before Ageing marginal samples (1 to 10)
    marginal_before1 VARCHAR(255), marginal_before2 VARCHAR(255), marginal_before3 VARCHAR(255),
    marginal_before4 VARCHAR(255), marginal_before5 VARCHAR(255), marginal_before6 VARCHAR(255),
    marginal_before7 VARCHAR(255), marginal_before8 VARCHAR(255), marginal_before9 VARCHAR(255),
    marginal_before10 VARCHAR(255),
    
    -- After Ageing marginal samples (1 to 10)
    marginal_after1 VARCHAR(255), marginal_after2 VARCHAR(255), marginal_after3 VARCHAR(255),
    marginal_after4 VARCHAR(255), marginal_after5 VARCHAR(255), marginal_after6 VARCHAR(255),
    marginal_after7 VARCHAR(255), marginal_after8 VARCHAR(255), marginal_after9 VARCHAR(255),
    marginal_after10 VARCHAR(255),
    
    tensile_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_tensile (call_no, lot_no)
);

-- ============================================================================
-- 3. ELONGATION (5 actual + 10 marginal = 15 per category)
-- ============================================================================
DROP TABLE IF EXISTS rail_final_elongation;
CREATE TABLE rail_final_elongation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Before Ageing actual samples (1 to 5)
    sample_before1 VARCHAR(255), sample_before2 VARCHAR(255), sample_before3 VARCHAR(255),
    sample_before4 VARCHAR(255), sample_before5 VARCHAR(255),
    
    -- After Ageing actual samples (1 to 5)
    sample_after1 VARCHAR(255), sample_after2 VARCHAR(255), sample_after3 VARCHAR(255),
    sample_after4 VARCHAR(255), sample_after5 VARCHAR(255),
    
    -- Before Ageing marginal samples (1 to 10)
    marginal_before1 VARCHAR(255), marginal_before2 VARCHAR(255), marginal_before3 VARCHAR(255),
    marginal_before4 VARCHAR(255), marginal_before5 VARCHAR(255), marginal_before6 VARCHAR(255),
    marginal_before7 VARCHAR(255), marginal_before8 VARCHAR(255), marginal_before9 VARCHAR(255),
    marginal_before10 VARCHAR(255),
    
    -- After Ageing marginal samples (1 to 10)
    marginal_after1 VARCHAR(255), marginal_after2 VARCHAR(255), marginal_after3 VARCHAR(255),
    marginal_after4 VARCHAR(255), marginal_after5 VARCHAR(255), marginal_after6 VARCHAR(255),
    marginal_after7 VARCHAR(255), marginal_after8 VARCHAR(255), marginal_after9 VARCHAR(255),
    marginal_after10 VARCHAR(255),
    
    elongation_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_elongation (call_no, lot_no)
);

-- ============================================================================
-- 4. MODULUS (3 actual + 6 marginal = 9 per category)
-- ============================================================================
DROP TABLE IF EXISTS rail_final_modulus;
CREATE TABLE rail_final_modulus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Before Ageing actual samples (1 to 3)
    sample_before1 VARCHAR(255), sample_before2 VARCHAR(255), sample_before3 VARCHAR(255),
    
    -- After Ageing actual samples (1 to 3)
    sample_after1 VARCHAR(255), sample_after2 VARCHAR(255), sample_after3 VARCHAR(255),
    
    -- Before Ageing marginal samples (1 to 6)
    marginal_before1 VARCHAR(255), marginal_before2 VARCHAR(255), marginal_before3 VARCHAR(255),
    marginal_before4 VARCHAR(255), marginal_before5 VARCHAR(255), marginal_before6 VARCHAR(255),
    
    -- After Ageing marginal samples (1 to 6)
    marginal_after1 VARCHAR(255), marginal_after2 VARCHAR(255), marginal_after3 VARCHAR(255),
    marginal_after4 VARCHAR(255), marginal_after5 VARCHAR(255), marginal_after6 VARCHAR(255),
    
    modulus_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_modulus (call_no, lot_no)
);

-- ============================================================================
-- 5. COMPRESSION SET (3 actual + 6 marginal = 9 per category)
-- ============================================================================
DROP TABLE IF EXISTS rail_final_compression_set;
CREATE TABLE rail_final_compression_set (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Initial Thickness (A) actual samples (1 to 3)
    sample_initial1 VARCHAR(255), sample_initial2 VARCHAR(255), sample_initial3 VARCHAR(255),
    
    -- Final Thickness (B) actual samples (1 to 3)
    sample_final1 VARCHAR(255), sample_final2 VARCHAR(255), sample_final3 VARCHAR(255),
    
    -- Initial Thickness (A) marginal samples (1 to 6)
    marginal_initial1 VARCHAR(255), marginal_initial2 VARCHAR(255), marginal_initial3 VARCHAR(255),
    marginal_initial4 VARCHAR(255), marginal_initial5 VARCHAR(255), marginal_initial6 VARCHAR(255),
    
    -- Final Thickness (B) marginal samples (1 to 6)
    marginal_final1 VARCHAR(255), marginal_final2 VARCHAR(255), marginal_final3 VARCHAR(255),
    marginal_final4 VARCHAR(255), marginal_final5 VARCHAR(255), marginal_final6 VARCHAR(255),
    
    compression_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_compression_set (call_no, lot_no)
);

-- ============================================================================
-- 6. TENSION SET (3 actual + 6 marginal = 9 per category)
-- ============================================================================
DROP TABLE IF EXISTS rail_final_tension_set;
CREATE TABLE rail_final_tension_set (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Initial Length (A) actual samples (1 to 3)
    sample_initial1 VARCHAR(255), sample_initial2 VARCHAR(255), sample_initial3 VARCHAR(255),
    
    -- Final Length (B) actual samples (1 to 3)
    sample_final1 VARCHAR(255), sample_final2 VARCHAR(255), sample_final3 VARCHAR(255),
    
    -- Initial Length (A) marginal samples (1 to 6)
    marginal_initial1 VARCHAR(255), marginal_initial2 VARCHAR(255), marginal_initial3 VARCHAR(255),
    marginal_initial4 VARCHAR(255), marginal_initial5 VARCHAR(255), marginal_initial6 VARCHAR(255),
    
    -- Final Length (B) marginal samples (1 to 6)
    marginal_final1 VARCHAR(255), marginal_final2 VARCHAR(255), marginal_final3 VARCHAR(255),
    marginal_final4 VARCHAR(255), marginal_final5 VARCHAR(255), marginal_final6 VARCHAR(255),
    
    tension_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_tension_set (call_no, lot_no)
);

-- ============================================================================
-- 7. LOAD TEST (2 actual pads + 4 marginal pads = 6 pads, 8 loads × L+R each)
-- ============================================================================
DROP TABLE IF EXISTS rail_final_load_test;
CREATE TABLE rail_final_load_test (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(50),
    vendor_code VARCHAR(50),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Actual Pad 1 L & R Gauges (8 loads each)
    pad1_l1 VARCHAR(50), pad1_l2 VARCHAR(50), pad1_l3 VARCHAR(50), pad1_l4 VARCHAR(50),
    pad1_l5 VARCHAR(50), pad1_l6 VARCHAR(50), pad1_l7 VARCHAR(50), pad1_l8 VARCHAR(50),
    pad1_r1 VARCHAR(50), pad1_r2 VARCHAR(50), pad1_r3 VARCHAR(50), pad1_r4 VARCHAR(50),
    pad1_r5 VARCHAR(50), pad1_r6 VARCHAR(50), pad1_r7 VARCHAR(50), pad1_r8 VARCHAR(50),
    
    -- Actual Pad 2 L & R Gauges
    pad2_l1 VARCHAR(50), pad2_l2 VARCHAR(50), pad2_l3 VARCHAR(50), pad2_l4 VARCHAR(50),
    pad2_l5 VARCHAR(50), pad2_l6 VARCHAR(50), pad2_l7 VARCHAR(50), pad2_l8 VARCHAR(50),
    pad2_r1 VARCHAR(50), pad2_r2 VARCHAR(50), pad2_r3 VARCHAR(50), pad2_r4 VARCHAR(50),
    pad2_r5 VARCHAR(50), pad2_r6 VARCHAR(50), pad2_r7 VARCHAR(50), pad2_r8 VARCHAR(50),
    
    -- Marginal Pad 1 L & R Gauges
    m_pad1_l1 VARCHAR(50), m_pad1_l2 VARCHAR(50), m_pad1_l3 VARCHAR(50), m_pad1_l4 VARCHAR(50),
    m_pad1_l5 VARCHAR(50), m_pad1_l6 VARCHAR(50), m_pad1_l7 VARCHAR(50), m_pad1_l8 VARCHAR(50),
    m_pad1_r1 VARCHAR(50), m_pad1_r2 VARCHAR(50), m_pad1_r3 VARCHAR(50), m_pad1_r4 VARCHAR(50),
    m_pad1_r5 VARCHAR(50), m_pad1_r6 VARCHAR(50), m_pad1_r7 VARCHAR(50), m_pad1_r8 VARCHAR(50),
    
    -- Marginal Pad 2 L & R Gauges
    m_pad2_l1 VARCHAR(50), m_pad2_l2 VARCHAR(50), m_pad2_l3 VARCHAR(50), m_pad2_l4 VARCHAR(50),
    m_pad2_l5 VARCHAR(50), m_pad2_l6 VARCHAR(50), m_pad2_l7 VARCHAR(50), m_pad2_l8 VARCHAR(50),
    m_pad2_r1 VARCHAR(50), m_pad2_r2 VARCHAR(50), m_pad2_r3 VARCHAR(50), m_pad2_r4 VARCHAR(50),
    m_pad2_r5 VARCHAR(50), m_pad2_r6 VARCHAR(50), m_pad2_r7 VARCHAR(50), m_pad2_r8 VARCHAR(50),
    
    -- Marginal Pad 3 L & R Gauges
    m_pad3_l1 VARCHAR(50), m_pad3_l2 VARCHAR(50), m_pad3_l3 VARCHAR(50), m_pad3_l4 VARCHAR(50),
    m_pad3_l5 VARCHAR(50), m_pad3_l6 VARCHAR(50), m_pad3_l7 VARCHAR(50), m_pad3_l8 VARCHAR(50),
    m_pad3_r1 VARCHAR(50), m_pad3_r2 VARCHAR(50), m_pad3_r3 VARCHAR(50), m_pad3_r4 VARCHAR(50),
    m_pad3_r5 VARCHAR(50), m_pad3_r6 VARCHAR(50), m_pad3_r7 VARCHAR(50), m_pad3_r8 VARCHAR(50),
    
    -- Marginal Pad 4 L & R Gauges
    m_pad4_l1 VARCHAR(50), m_pad4_l2 VARCHAR(50), m_pad4_l3 VARCHAR(50), m_pad4_l4 VARCHAR(50),
    m_pad4_l5 VARCHAR(50), m_pad4_l6 VARCHAR(50), m_pad4_l7 VARCHAR(50), m_pad4_l8 VARCHAR(50),
    m_pad4_r1 VARCHAR(50), m_pad4_r2 VARCHAR(50), m_pad4_r3 VARCHAR(50), m_pad4_r4 VARCHAR(50),
    m_pad4_r5 VARCHAR(50), m_pad4_r6 VARCHAR(50), m_pad4_r7 VARCHAR(50), m_pad4_r8 VARCHAR(50),
    
    load_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_load_test (call_no, lot_no)
);

-- ============================================================================
-- 8. ELECTRICAL RESISTANCE (3 actual + 6 marginal = 9 per direction)
-- ============================================================================
DROP TABLE IF EXISTS rail_final_electrical_resistance;
CREATE TABLE rail_final_electrical_resistance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Actual: Before Immersion Forward (S1 to S3)
    s1_before_forward VARCHAR(50), s2_before_forward VARCHAR(50), s3_before_forward VARCHAR(50),
    -- Actual: Before Immersion Reverse (S1 to S3)
    s1_before_reverse VARCHAR(50), s2_before_reverse VARCHAR(50), s3_before_reverse VARCHAR(50),
    -- Actual: After Immersion Forward (S1 to S3)
    s1_after_forward VARCHAR(50), s2_after_forward VARCHAR(50), s3_after_forward VARCHAR(50),
    -- Actual: After Immersion Reverse (S1 to S3)
    s1_after_reverse VARCHAR(50), s2_after_reverse VARCHAR(50), s3_after_reverse VARCHAR(50),
    
    -- Marginal: Before Immersion Forward (M1 to M6)
    m1_before_forward VARCHAR(50), m2_before_forward VARCHAR(50), m3_before_forward VARCHAR(50),
    m4_before_forward VARCHAR(50), m5_before_forward VARCHAR(50), m6_before_forward VARCHAR(50),
    -- Marginal: Before Immersion Reverse (M1 to M6)
    m1_before_reverse VARCHAR(50), m2_before_reverse VARCHAR(50), m3_before_reverse VARCHAR(50),
    m4_before_reverse VARCHAR(50), m5_before_reverse VARCHAR(50), m6_before_reverse VARCHAR(50),
    -- Marginal: After Immersion Forward (M1 to M6)
    m1_after_forward VARCHAR(50), m2_after_forward VARCHAR(50), m3_after_forward VARCHAR(50),
    m4_after_forward VARCHAR(50), m5_after_forward VARCHAR(50), m6_after_forward VARCHAR(50),
    -- Marginal: After Immersion Reverse (M1 to M6)
    m1_after_reverse VARCHAR(50), m2_after_reverse VARCHAR(50), m3_after_reverse VARCHAR(50),
    m4_after_reverse VARCHAR(50), m5_after_reverse VARCHAR(50), m6_after_reverse VARCHAR(50),
    
    electrical_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_electrical_resistance (call_no, lot_no)
);

-- ============================================================================
-- 9. SPECIFIC GRAVITY (3 actual + 6 marginal = 9 per compound per weight)
-- ============================================================================
DROP TABLE IF EXISTS rail_final_specific_gravity;
CREATE TABLE rail_final_specific_gravity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    call_no VARCHAR(255) NOT NULL,
    lot_no VARCHAR(255) NOT NULL,
    plant_id VARCHAR(255),
    vendor_code VARCHAR(255),
    shift VARCHAR(50),
    railpad_type VARCHAR(255),
    offered_qty INT,
    
    -- Actual: Compound A Air Weight (S1 to S3)
    s1_a_air VARCHAR(50), s2_a_air VARCHAR(50), s3_a_air VARCHAR(50),
    -- Actual: Compound A Water Weight (S1 to S3)
    s1_a_water VARCHAR(50), s2_a_water VARCHAR(50), s3_a_water VARCHAR(50),
    -- Actual: Compound B Air Weight (S1 to S3)
    s1_b_air VARCHAR(50), s2_b_air VARCHAR(50), s3_b_air VARCHAR(50),
    -- Actual: Compound B Water Weight (S1 to S3)
    s1_b_water VARCHAR(50), s2_b_water VARCHAR(50), s3_b_water VARCHAR(50),
    
    -- Marginal: Compound A Air Weight (M1 to M6)
    m1_a_air VARCHAR(50), m2_a_air VARCHAR(50), m3_a_air VARCHAR(50),
    m4_a_air VARCHAR(50), m5_a_air VARCHAR(50), m6_a_air VARCHAR(50),
    -- Marginal: Compound A Water Weight (M1 to M6)
    m1_a_water VARCHAR(50), m2_a_water VARCHAR(50), m3_a_water VARCHAR(50),
    m4_a_water VARCHAR(50), m5_a_water VARCHAR(50), m6_a_water VARCHAR(50),
    -- Marginal: Compound B Air Weight (M1 to M6)
    m1_b_air VARCHAR(50), m2_b_air VARCHAR(50), m3_b_air VARCHAR(50),
    m4_b_air VARCHAR(50), m5_b_air VARCHAR(50), m6_b_air VARCHAR(50),
    -- Marginal: Compound B Water Weight (M1 to M6)
    m1_b_water VARCHAR(50), m2_b_water VARCHAR(50), m3_b_water VARCHAR(50),
    m4_b_water VARCHAR(50), m5_b_water VARCHAR(50), m6_b_water VARCHAR(50),
    
    sg_status VARCHAR(50),
    not_ok_count INT,
    remarks TEXT,
    
    created_by BIGINT,
    created_date DATETIME,
    updated_by BIGINT,
    updated_date DATETIME,
    
    UNIQUE KEY uk_rail_final_specific_gravity (call_no, lot_no)
);
