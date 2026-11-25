-- =====================================================================
-- V20261124_003 : Rail-mill (Rites) module tables  ->  com.sarthi.Sms
-- Source: Kishore's Rites backend (D:\New Rites\back-end, branch Kishore-backend-Rites)
--
-- * CREATE TABLE IF NOT EXISTS only - safe to re-run, never drops or changes data.
-- * Generated from the ported JPA entities (what the code reads/writes); column types taken
--   from the Rites derites.sql where it defines them (DECIMAL precision, ENUM, JSON, ...).
-- * NOT included on purpose (SARTHI isolation):
--     - user_master / user_role_master and every ALTER on them (SARTHI's own login tables)
--     - FOREIGN KEYs to the Rites user_master (user_id columns are kept as plain values)
--     - the 11 SMS tables that already exist (sms_duty, sms_duty_heat, sms_heat_detail,
--       heat_detail_sms2/3, duty_heat_sms2/3, bloom_detail, bloom_detail_sms2/3, duty_sequence)
-- * No seed data: the Rites SQL has none (Calibration instruments are entered from the app).
-- Run on UAT first, then PROD.
-- =====================================================================

SET NAMES utf8mb4;

-- entity: entity/qct/QctMasterEntity.java
CREATE TABLE IF NOT EXISTS `QCT_MASTER` (
  `qct_id` INT NOT NULL AUTO_INCREMENT,
  `heat_No` VARCHAR(20) NOT NULL,
  `strand_No` INT NOT NULL,
  `sample_id` VARCHAR(30) NULL,
  `mill` VARCHAR(20) NULL,
  `rail_grade` VARCHAR(20) NULL,
  `rail_section` VARCHAR(20) NULL,
  `qct_Type` VARCHAR(50) NOT NULL,
  `duty_Id` VARCHAR(50) NOT NULL,
  `created_At` DATETIME NOT NULL,
  PRIMARY KEY (`qct_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/calibration/CalibrationDetailEntity.java
CREATE TABLE IF NOT EXISTS `calibration_detail` (
  `serial_number` VARCHAR(50) NOT NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  `instrument_category` VARCHAR(50) NOT NULL,
  `instrument` VARCHAR(50) NOT NULL,
  `detail` VARCHAR(50) NOT NULL,
  `rail_section` VARCHAR(50) NULL,
  `notification_after_days` INT NULL,
  `calibration_date` DATE NOT NULL,
  `result` VARCHAR(50) NOT NULL,
  `calibration_valid_upto` DATE NULL,
  PRIMARY KEY (`serial_number`, `duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/calibration/CalibrationDutyEntity.java
CREATE TABLE IF NOT EXISTS `calibration_duty` (
  `duty_id` VARCHAR(255) NOT NULL,
  `user_id` VARCHAR(255) NOT NULL,
  `shift` VARCHAR(10) NOT NULL,
  `date` DATE NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NULL,
  `shift_remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/bsp/ChemTestRecord.java
CREATE TABLE IF NOT EXISTS `chem_test_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `ord` VARCHAR(10) NULL,
  `analysis` VARCHAR(50) NULL,
  `c_result` VARCHAR(20) NULL,
  `mn_result` VARCHAR(20) NULL,
  `p_result` VARCHAR(20) NULL,
  `s_result` VARCHAR(20) NULL,
  `si_result` VARCHAR(20) NULL,
  `al_result` VARCHAR(20) NULL,
  `h_result` VARCHAR(20) NULL,
  `cu_result` VARCHAR(20) NULL,
  `cr_result` VARCHAR(20) NULL,
  `v_result` VARCHAR(20) NULL,
  `ni_result` VARCHAR(20) NULL,
  `mo_result` VARCHAR(20) NULL,
  `n_result` VARCHAR(20) NULL,
  `o_result` VARCHAR(20) NULL,
  `sb_result` VARCHAR(20) NULL,
  `sn_result` VARCHAR(20) NULL,
  `ti_result` VARCHAR(20) NULL,
  `nb_result` VARCHAR(20) NULL,
  `ct_result` VARCHAR(20) NULL,
  `cmn_result` VARCHAR(20) NULL,
  `retest_step` VARCHAR(10) NULL,
  `remarks` VARCHAR(255) NULL,
  `type` VARCHAR(50) NULL,
  `lot_no` VARCHAR(50) NULL,
  `strand_no` VARCHAR(50) NULL,
  `heat_no` VARCHAR(100) NULL,
  `mill` VARCHAR(50) NULL,
  `sms` VARCHAR(50) NULL,
  `created_at` DATE NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/testing/CompletedTest.java
CREATE TABLE IF NOT EXISTS `completed_tests` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `duty_id` VARCHAR(255) NOT NULL,
  `test_type` VARCHAR(50) NOT NULL,
  `heat_number` VARCHAR(50) NOT NULL,
  `strand_number` INT NULL,
  `mill` VARCHAR(50) NULL,
  `rail_grade` VARCHAR(50) NULL,
  `sample_type` VARCHAR(50) NULL,
  `sample_lot` VARCHAR(50) NULL,
  `sample_id` VARCHAR(50) NULL,
  `sample_identification` VARCHAR(255) NULL,
  `chemical_ladle_status` VARCHAR(50) NULL,
  `chemcial_product_status` VARCHAR(50) NULL,
  `fwt_status` VARCHAR(50) NULL,
  `macro_status` VARCHAR(50) NULL,
  `micro_status` VARCHAR(50) NULL,
  `micro_structure_pearlitic` BIT(1) NULL,
  `tensile_status` VARCHAR(50) NULL,
  `tensile_yield` DECIMAL(10,2) NULL,
  `tensile_uts` DECIMAL(10,2) NULL,
  `tensile_ei` DECIMAL(10,2) NULL,
  `tensile_foot_status` VARCHAR(50) NULL,
  `tensile_foot_yield` DECIMAL(10,2) NULL,
  `tensile_foot_uts` DECIMAL(10,2) NULL,
  `tensile_foot_ei` DECIMAL(10,2) NULL,
  `harness_status` VARCHAR(50) NULL,
  `hardenss_1a` DECIMAL(10,2) NULL,
  `hardenss_1b` DECIMAL(10,2) NULL,
  `hardenss_1c` DECIMAL(10,2) NULL,
  `hardenss_2a` DECIMAL(10,2) NULL,
  `hardenss_2b` DECIMAL(10,2) NULL,
  `hardenss_3` DECIMAL(10,2) NULL,
  `hardenss_4a` DECIMAL(10,2) NULL,
  `hardenss_4b` DECIMAL(10,2) NULL,
  `hardenss_rs` DECIMAL(10,2) NULL,
  `ir_status` VARCHAR(50) NULL,
  `ir_sulphide` DECIMAL(10,2) NULL,
  `ir_aluminate` DECIMAL(10,2) NULL,
  `ir_silicate` DECIMAL(10,2) NULL,
  `ir_oxide` DECIMAL(10,2) NULL,
  `decarb_status` VARCHAR(50) NULL,
  `decarb_1` DECIMAL(10,2) NULL,
  `decarb_2` DECIMAL(10,2) NULL,
  `decarb_3` DECIMAL(10,2) NULL,
  `n2_status` VARCHAR(50) NULL,
  `n2` DECIMAL(10,2) NULL,
  `o2_status` VARCHAR(50) NULL,
  `o2` DECIMAL(10,2) NULL,
  `sp_status` VARCHAR(50) NULL,
  `sp_value` DECIMAL(10,2) NULL,
  `rsh_status` VARCHAR(50) NULL,
  `rsh_value` DECIMAL(10,2) NULL,
  `ph_status` VARCHAR(50) NULL,
  `ph_value` DECIMAL(10,2) NULL,
  `remarks` TEXT NULL,
  `created_at` TIMESTAMP NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/dashboard/DimensionalInspectionEntity.java
CREATE TABLE IF NOT EXISTS `dimensional_inspection` (
  `inspection_id` INT NOT NULL AUTO_INCREMENT,
  `rail_id` VARCHAR(100) NOT NULL,
  `camera_id` INT NOT NULL,
  `base_image_path` VARCHAR(255) NOT NULL,
  `edge_diff` DECIMAL(10, 3) NULL,
  `chop_diff` DECIMAL(10, 3) NULL,
  `image_diff` DECIMAL(10, 3) NULL,
  `actual_status` VARCHAR(10) NULL,
  `result_status` VARCHAR(10) NULL,
  `confusion_classifier` VARCHAR(10) NULL,
  `inspected_at` TIMESTAMP NOT NULL,
  `operator_id` VARCHAR(255) NULL,
  `duty_id` VARCHAR(255) NULL,
  `shift` VARCHAR(5) NULL,
  `defect_type` JSON NULL,
  `distance_from_head` DECIMAL(10, 3) NULL,
  `dimension_deviation` DECIMAL(10, 5) NULL,
  `no_of_dimension_variation` INT NULL,
  `camera_name` VARCHAR(50) NULL,
  `inspected_image_path` VARCHAR(255) NULL,
  PRIMARY KEY (`inspection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/bsp/FwtTestRecord.java
CREATE TABLE IF NOT EXISTS `fwt_test_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `test_name` VARCHAR(100) NULL,
  `lot_no` VARCHAR(50) NULL,
  `sample_no` VARCHAR(100) NULL,
  `strand_no` VARCHAR(20) NULL,
  `test` VARCHAR(50) NULL,
  `sample_test_dtm` VARCHAR(50) NULL,
  `deflection_mm` VARCHAR(20) NULL,
  `remarks` VARCHAR(255) NULL,
  `retest_step` VARCHAR(50) NULL,
  `heat_no` VARCHAR(50) NULL,
  `mill` VARCHAR(50) NULL,
  `sms` VARCHAR(50) NULL,
  `rail_grade` VARCHAR(100) NULL,
  `section` VARCHAR(50) NULL,
  `heat_judgment_dt` VARCHAR(50) NULL,
  `heat_status` VARCHAR(100) NULL,
  `heat_judgment_dtm` VARCHAR(50) NULL,
  `created_at` DATE NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/heatnumber/HeatnumberInspectionEntity.java
CREATE TABLE IF NOT EXISTS `heatnumber_inspection` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `rail_id` VARCHAR(50) NOT NULL UNIQUE,
  `shift` VARCHAR(5) NULL,
  `heatnumber` VARCHAR(15) NULL,
  `ocr_hn` VARCHAR(15) NULL,
  `ai_status` BIT(1) NULL,
  `admin_status` BIT(1) NULL,
  `created_at` DATETIME(6) NULL,
  `updated_at` DATETIME(6) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/HtSequenceEntity.java
CREATE TABLE IF NOT EXISTS `ht_sequence` (
  `rail_id` VARCHAR(50) NOT NULL,
  `bloom_quality` ENUM("CC", "CO") NULL,
  `ht_status` BIT(1) NULL,
  `created_by` VARCHAR(50) NULL,
  `created_at` DATETIME NOT NULL,
  `batch_open` BIT(1) NULL,
  `batch_end_duty_id` VARCHAR(50) NULL,
  PRIMARY KEY (`rail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/HtSequenceHeatEntity.java
CREATE TABLE IF NOT EXISTS `ht_sequence_heat` (
  `heat_number` VARCHAR(255) NOT NULL,
  `batch_create_duty_id` VARCHAR(255) NULL,
  `batch_end_duty_id` VARCHAR(255) NULL,
  `ht_sequence_batch_complete` BIT(1) NULL,
  PRIMARY KEY (`heat_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/HtSequenceHeatRailEntity.java
CREATE TABLE IF NOT EXISTS `ht_sequence_heat_rail` (
  `rail_id` VARCHAR(255) NOT NULL,
  `heat_number` VARCHAR(255) NULL,
  `bloom_quality` VARCHAR(255) NULL,
  `ht_status` BIT(1) NULL,
  `duty_id` VARCHAR(255) NULL,
  `created_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`rail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/bsp/MacroTestRecord.java
CREATE TABLE IF NOT EXISTS `macro_test_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `test_name` VARCHAR(100) NULL,
  `lot_no` VARCHAR(50) NULL,
  `sample_no` VARCHAR(100) NULL,
  `strand_no` VARCHAR(50) NULL,
  `test` VARCHAR(50) NULL,
  `sample_test_dtm` VARCHAR(50) NULL,
  `visual_exam` VARCHAR(50) NULL,
  `sulphur_print` VARCHAR(50) NULL,
  `limit_print` VARCHAR(50) NULL,
  `retest_step` VARCHAR(100) NULL,
  `heat_no` VARCHAR(100) NULL,
  `mill` VARCHAR(50) NULL,
  `sms` VARCHAR(50) NULL,
  `rail_grade` VARCHAR(100) NULL,
  `section` VARCHAR(50) NULL,
  `heat_judgment_dt` VARCHAR(50) NULL,
  `heat_status` VARCHAR(100) NULL,
  `heat_judgment_dtm` VARCHAR(50) NULL,
  `created_at` DATE NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/ndt/NdtCalibrationEntity.java
CREATE TABLE IF NOT EXISTS `ndt_calibration` (
  `calibration_id` INT NOT NULL AUTO_INCREMENT,
  `duty_id` VARCHAR(50) NOT NULL,
  `ut` BIT(1) NOT NULL,
  `ct` BIT(1) NOT NULL,
  `fmg` BIT(1) NOT NULL,
  `OSIRIS` BIT(1) NOT NULL,
  `calibration_completion_time` TIME NOT NULL,
  `calibration_speed` DECIMAL(10, 2) NOT NULL,
  `rcl_rep_name` VARCHAR(50) NOT NULL,
  `calibration_round_remarks` VARCHAR(50) NOT NULL,
  `create_date` DATETIME NOT NULL,
  PRIMARY KEY (`calibration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/ndt/NdtDutyEntity.java
CREATE TABLE IF NOT EXISTS `ndt_duty` (
  `duty_id` VARCHAR(50) NOT NULL,
  `user_id` VARCHAR(50) NOT NULL,
  `date` DATE NOT NULL,
  `shift` VARCHAR(10) NOT NULL,
  `mill` VARCHAR(10) NOT NULL,
  `ndt` VARCHAR(50) NOT NULL,
  `rail_grade` VARCHAR(10) NOT NULL,
  `rail_section` VARCHAR(10) NOT NULL,
  `shift_remarks` VARCHAR(50) NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NULL,
  `calibration_remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/ndt/NdtDutyClbPrevDataEntity.java
CREATE TABLE IF NOT EXISTS `ndt_duty_clb_prev_data` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `duty_id` VARCHAR(50) NOT NULL,
  `calibration_id` INT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/ndt/NdtShiftSummaryEntity.java
CREATE TABLE IF NOT EXISTS `ndt_shift_summary` (
  `duty_id` VARCHAR(50) NOT NULL,
  `rolling_table_speed` DECIMAL(10, 2) NOT NULL,
  `total_rails_inspected` INT NOT NULL,
  `total_ut_marked_rails` INT NULL,
  `total_ect_marked_rails` INT NULL,
  `body_straightness_result_a` INT NOT NULL,
  `body_straightness_result_a_1` INT NOT NULL,
  `body_straightness_result_marked` INT NOT NULL,
  `total_profile_marked` INT NULL,
  `rail_length` INT NULL,
  `total_ut_ok_rails` INT NULL,
  `total_ut_suspected_rails` INT NULL,
  `total_ut_rejected_rails` INT NULL,
  `total_recycled_rails` INT NULL,
  `ect_suspected_rails` INT NULL,
  `remarks` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctCenterLineMasterEntity.java
CREATE TABLE IF NOT EXISTS `qct_center_line_master` (
  `qct_id` INT NOT NULL,
  `duty_id` VARCHAR(50) NULL,
  `remarks` VARCHAR(50) NULL,
  `mean_rsh` FLOAT NULL,
  `mean_variation` DECIMAL(19,2) NULL,
  `hardness_reading_count` INT NULL,
  `max_variation` DECIMAL(10, 2) NULL,
  PRIMARY KEY (`qct_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctDetailEntity.java
CREATE TABLE IF NOT EXISTS `qct_detail` (
  `heat_no` INT NOT NULL,
  `strand_no` INT NOT NULL,
  `sample_id` VARCHAR(255) NOT NULL,
  `qct_type` VARCHAR(255) NOT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `duty_id` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`heat_no`, `strand_no`, `sample_id`, `qct_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctDutyEntity.java
CREATE TABLE IF NOT EXISTS `qct_duty` (
  `duty_id` VARCHAR(50) NOT NULL,
  `user_id` VARCHAR(50) NOT NULL,
  `date` DATE NOT NULL,
  `shift` VARCHAR(10) NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NULL,
  `shift_remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctFatigueDetailEntity.java
CREATE TABLE IF NOT EXISTS `qct_fatigue_detail` (
  `qct_id` INTEGER NOT NULL,
  `duty_Id` VARCHAR(50) NOT NULL,
  `is_broken` BIT(1) NULL,
  PRIMARY KEY (`qct_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctFatigueMasterEntity.java
CREATE TABLE IF NOT EXISTS `qct_fatigue_master` (
  `duty_Id` VARCHAR(50) NOT NULL,
  `strain` DECIMAL(10, 2) NULL,
  `frequency` INT NULL,
  `stress` DECIMAL(10, 2) NULL,
  `cycle_Endured` DECIMAL(10, 2) NULL,
  `remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctFcgrMasterEntity.java
CREATE TABLE IF NOT EXISTS `qct_fcgr_master` (
  `qct_id` INT NOT NULL,
  `duty_id` VARCHAR(50) NULL,
  `mpam_10` FLOAT NULL,
  `mpam_13` FLOAT NULL,
  `remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`qct_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctFractureToughnessMasterEntity.java
CREATE TABLE IF NOT EXISTS `qct_fracture_toughness_master` (
  `qct_id` INT NOT NULL,
  `duty_Id` VARCHAR(50) NULL,
  `parameter` VARCHAR(20) NULL,
  `value` FLOAT NULL,
  `remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`qct_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctResidualMasterEntity.java
CREATE TABLE IF NOT EXISTS `qct_residual_master` (
  `duty_Id` VARCHAR(50) NOT NULL,
  `qct_id` INT NULL,
  `stress` DECIMAL(10, 2) NULL,
  `stress_nature` VARCHAR(20) NULL,
  `remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_Id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctTestingDetailEntity.java
CREATE TABLE IF NOT EXISTS `qct_testing_detail_table` (
  `heat_no` INT NOT NULL,
  `strand_no` INT NOT NULL,
  `sample_id` VARCHAR(255) NOT NULL,
  `qct_type` VARCHAR(255) NOT NULL,
  `duty_id` VARCHAR(255) NOT NULL,
  `fatique_result` VARCHAR(255) NULL,
  `stress_mpa` FLOAT NOT NULL,
  `nature_of_stress` VARCHAR(255) NULL,
  `fcgr_da_dn_10` FLOAT NULL,
  `fcgr_da_dn_13_5` FLOAT NULL,
  `value_reported_as` VARCHAR(255) NULL,
  `value_of_reported_parameter` FLOAT NULL,
  `no_of_hardness` INT NULL,
  `mean_rsh` FLOAT NULL,
  `mx_variation` FLOAT NULL,
  PRIMARY KEY (`heat_no`, `strand_no`, `sample_id`, `qct_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/qct/QctTestingMasterEntity.java
CREATE TABLE IF NOT EXISTS `qct_testing_master_table` (
  `duty_id` VARCHAR(255) NOT NULL,
  `qct_type` VARCHAR(255) NOT NULL,
  `strain` FLOAT NULL,
  `frequency_hz` INT NULL,
  `stress` FLOAT NULL,
  `cycle_endured` FLOAT NULL,
  `remarks` VARCHAR(255) NULL,
  `number_of_samples_reported_as_kq` INT NULL,
  `mean_of_values_kq` INT NOT NULL,
  PRIMARY KEY (`duty_id`, `qct_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/RetestSample.java
CREATE TABLE IF NOT EXISTS `retest_sample` (
  `retest_sample_id` INT NOT NULL AUTO_INCREMENT,
  `rail_grade` VARCHAR(50) NULL,
  `duty_id` VARCHAR(50) NULL,
  `sample_id` VARCHAR(50) NULL,
  `sample_lot` VARCHAR(50) NULL,
  `heat_number` VARCHAR(50) NULL,
  `strand_number` VARCHAR(50) NULL,
  `test_type` VARCHAR(50) NULL,
  `create_date` DATETIME NULL,
  PRIMARY KEY (`retest_sample_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/RollingHeatDtlEntity.java
CREATE TABLE IF NOT EXISTS `rolling_control_heat_detail` (
  `heat_number` VARCHAR(50) NOT NULL,
  `sample_number` INT NOT NULL,
  `duty_id` VARCHAR(50) NULL,
  `timing` TIME NOT NULL,
  `sample_location` VARCHAR(50) NOT NULL,
  `height` DECIMAL(10,2) NOT NULL,
  `head` VARCHAR(10) NOT NULL,
  `flange` DECIMAL(10,2) NOT NULL,
  `web` DECIMAL(10,2) NOT NULL,
  `asy` VARCHAR(50) NULL,
  `foot_toe` VARCHAR(50) NULL,
  `fishing_height` VARCHAR(50) NULL,
  `weight` DECIMAL(10,2) NULL,
  `crown_profile` VARCHAR(50) NULL,
  `foot_flatness` VARCHAR(50) NULL,
  `remark` VARCHAR(50) NULL,
  PRIMARY KEY (`heat_number`, `sample_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/RollingDutyEntity.java
CREATE TABLE IF NOT EXISTS `rolling_duty` (
  `duty_id` VARCHAR(50) NOT NULL,
  `user_id` VARCHAR(50) NOT NULL,
  `date` DATE NOT NULL,
  `shift` VARCHAR(10) NOT NULL,
  `mill` VARCHAR(10) NOT NULL,
  `rail_grade` VARCHAR(50) NOT NULL,
  `rail_section` VARCHAR(50) NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NULL,
  `vernier_number` VARCHAR(20) NULL,
  `micrometer_number` VARCHAR(20) NULL,
  `weighing_machine` VARCHAR(20) NULL,
  `branding` VARCHAR(50) NULL,
  `number_of_gauges` INT NULL,
  `calibration_validity_vernier` DATE NULL,
  `calibration_validity_micrometer` DATE NULL,
  `calibration_validity_weighing_machine` DATE NULL,
  `shift_remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/FinishingVerificationHotStampingEntity.java
CREATE TABLE IF NOT EXISTS `rolling_finishing_verification_hot_stamping` (
  `heat_number` VARCHAR(50) NOT NULL,
  `brand` VARCHAR(50) NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`heat_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/FinishingVerificationPreCamberingEntity.java
CREATE TABLE IF NOT EXISTS `rolling_finishing_verification_pre_cambering` (
  `heat_number` VARCHAR(50) NOT NULL,
  `is_satisfactory` BIT(1) NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`heat_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/FinishingVerificationStraignteningMachineEntity.java
CREATE TABLE IF NOT EXISTS `rolling_finishing_verification_straightening_machine` (
  `duty_id` VARCHAR(50) NOT NULL,
  `sm_r1_hrsm` VARCHAR(50) NULL,
  `sm_r4_hrsm` VARCHAR(50) NULL,
  `sm_r6_hrsm` VARCHAR(50) NULL,
  `sm_r8_hrsm` VARCHAR(50) NULL,
  `sm_r1_vrsm` VARCHAR(50) NULL,
  `sm_r4_vrsm` VARCHAR(50) NULL,
  `sm_r6_vrsm` VARCHAR(50) NULL,
  `sm_r8_vrsm` VARCHAR(50) NULL,
  `sm_is_satisfactory` BIT(1) NULL,
  `sm_sr_r1_hrsm` VARCHAR(50) NULL,
  `sm_sr_r2_hrsm` VARCHAR(50) NULL,
  `sm_sr_r3_hrsm` VARCHAR(50) NULL,
  `sm_sr_r4_hrsm` VARCHAR(50) NULL,
  `sm_sr_r1_vrsm` VARCHAR(50) NULL,
  `sm_sr_r2_vrsm` VARCHAR(50) NULL,
  `sm_sr_r3_vrsm` VARCHAR(50) NULL,
  `sm_sr_r4_vrsm` VARCHAR(50) NULL,
  `sm_sr_is_satisfactory` BIT(1) NULL,
  `sm_lr_r1_hrsm` VARCHAR(50) NULL,
  `sm_lr_r2_hrsm` VARCHAR(50) NULL,
  `sm_lr_r3_hrsm` VARCHAR(50) NULL,
  `sm_lr_r4_hrsm` VARCHAR(50) NULL,
  `sm_lr_r1_vrsm` VARCHAR(50) NULL,
  `sm_lr_r2_vrsm` VARCHAR(50) NULL,
  `sm_lr_r3_vrsm` VARCHAR(50) NULL,
  `sm_lr_r4_vrsm` VARCHAR(50) NULL,
  `sm_lr_is_satisfactory` BIT(1) NULL,
  `straigtness_remarks` VARCHAR(50) NULL,
  `remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/TemperatureObservationEntity.java
CREATE TABLE IF NOT EXISTS `rolling_temperature_observation` (
  `duty_id` VARCHAR(50) NOT NULL,
  `created_at` TIME NOT NULL,
  `soaking_zone_temperature` INT NULL,
  `finishing_temperature` INT NULL,
  `remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/AcptTestRelationEntity.java
CREATE TABLE IF NOT EXISTS `rolling_test_sample_acceptance_relation` (
  `duty_id` VARCHAR(50) NOT NULL,
  `rolling_test_sample_id` INT NOT NULL,
  `tests_marked` VARCHAR(250) NULL,
  PRIMARY KEY (`duty_id`, `rolling_test_sample_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/VerificationBloomYardEntity.java
CREATE TABLE IF NOT EXISTS `rolling_verification_bloom_yard_detail` (
  `heat_number` VARCHAR(50) NOT NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  `internal_transfer_verification` BIT(1) NULL,
  `heat_register_verification` BIT(1) NULL,
  `length` DECIMAL(10, 2) NULL,
  `defect` VARCHAR(100) NULL,
  `remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`heat_number`, `duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/rolling/VerificationChargingEntity.java
CREATE TABLE IF NOT EXISTS `rolling_verification_charging_detail` (
  `duty_id` VARCHAR(50) NOT NULL,
  `heat_number` VARCHAR(50) NOT NULL,
  `length` DECIMAL(10, 2) NOT NULL,
  `defect` VARCHAR(50) NULL,
  `bloom_tracking_verification` BIT(1) NOT NULL,
  `hot_blooms_descaling` BIT(1) NOT NULL,
  `remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`heat_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/shortrail/ShortRailDutyEntity.java
CREATE TABLE IF NOT EXISTS `short_rail_duty` (
  `duty_id` VARCHAR(255) NOT NULL,
  `user_id` VARCHAR(255) NOT NULL,
  `date` DATE NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NULL,
  `shift` VARCHAR(10) NOT NULL,
  `shift_remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/shortrail/ShortRailMasterEntity.java
CREATE TABLE IF NOT EXISTS `short_rail_master` (
  `Rail_Section_Inspected` VARCHAR(50) NOT NULL,
  `Rail_Grade_Inspected` VARCHAR(50) NOT NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  `Acceptance_26m_CL_A` INT NULL,
  `Acceptance_26m_CL_A_0_1` INT NULL,
  `Acceptance_26m_CL_B` INT NULL,
  `Acceptance_26m_IU` INT NULL,
  `Acceptance_25m_CL_A` INT NULL,
  `Acceptance_25m_CL_A_0_1` INT NULL,
  `Acceptance_25m_CL_B` INT NULL,
  `Acceptance_25m_IU` INT NULL,
  `Acceptance_24m_CL_A` INT NULL,
  `Acceptance_24m_CL_A_0_1` INT NULL,
  `Acceptance_24m_CL_B` INT NULL,
  `Acceptance_24m_IU` INT NULL,
  `Acceptance_13m_CL_A` INT NULL,
  `Acceptance_13m_CL_A_0_1` INT NULL,
  `Acceptance_13m_CL_B` INT NULL,
  `Acceptance_13m_IU` INT NULL,
  `Acceptance_12m_CL_A` INT NULL,
  `Acceptance_12m_CL_A_0_1` INT NULL,
  `Acceptance_12m_CL_B` INT NULL,
  `Acceptance_12m_IU` INT NULL,
  `Acceptance_11m_CL_A` INT NULL,
  `Acceptance_11m_CL_A_0_1` INT NULL,
  `Acceptance_11m_CL_B` INT NULL,
  `Acceptance_11m_IU` INT NULL,
  `Acceptance_10m_CL_A` INT NULL,
  `Acceptance_10m_CL_A_0_1` INT NULL,
  `Acceptance_10m_CL_B` INT NULL,
  `Acceptance_10m_IU` INT NULL,
  `rej_ref_rejection_26m` INT NULL,
  `rej_ref_cutbar_26m` INT NULL,
  `rej_ref_refinish_26m` INT NULL,
  `rej_ref_rejection_13m` INT NULL,
  `rej_ref_cutbar_13m` INT NULL,
  `rej_ref_refinish_13m` INT NULL,
  `utnp_rejection_26m` INT NULL,
  `utnp_cutbar_26m` INT NULL,
  `utnp_refinish_26m` INT NULL,
  `utnp_rejection_13m` INT NULL,
  `utnp_cutbar_13m` INT NULL,
  `utnp_refinish_13m` INT NULL,
  `created_at` DATETIME NULL,
  `Remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`Rail_Section_Inspected`, `Rail_Grade_Inspected`, `duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/shortrail/ShortRailRejectionAnalysisEntity.java
CREATE TABLE IF NOT EXISTS `short_rail_rejection_analysis` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `duty_id` VARCHAR(50) NOT NULL,
  `Rail_Section_Inspected` VARCHAR(50) NOT NULL,
  `Rail_Grade_Inspected` VARCHAR(50) NOT NULL,
  `defect_26m` VARCHAR(30) NULL,
  `defect_26m_count` INT NULL,
  `defect_13m` VARCHAR(30) NULL,
  `defect_13m_count` INT NULL,
  `created_at` DATETIME NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/sms/ShiftSummaryEntity.java
CREATE TABLE IF NOT EXISTS `sms_shift_summary_master` (
  `duty_id` VARCHAR(255) NOT NULL,
  `is_ems_functioning` BIT(1) NULL,
  `is_slag_detector_functioning` BIT(1) NULL,
  `is_amlc_functioning` BIT(1) NULL,
  `is_hydrogen_measurement_automatic` BIT(1) NULL,
  `is_ladle_to_tundish_used` BIT(1) NULL,
  `is_tundish_to_mould_used` BIT(1) NULL,
  `make_of_casting_powder` VARCHAR(255) NULL,
  `make_of_hydris_probe` VARCHAR(255) NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/dashboard/SurfaceInspectionEntity.java
CREATE TABLE IF NOT EXISTS `surface_inspection` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `annotated_image` VARCHAR(255) NOT NULL,
  `rail_id` VARCHAR(50) NOT NULL,
  `shift` VARCHAR(5) NULL,
  `distance` INT NULL,
  `camera` INT NOT NULL,
  `label_index` INT NULL,
  `label_name` VARCHAR(255) NULL,
  `confidence` DECIMAL(10, 5) NULL,
  `created_at` TIMESTAMP NULL,
  `updated_at` TIMESTAMP NULL,
  `ai_status` INT NULL,
  `admin_status` INT NULL,
  `camera_name` VARCHAR(50) NULL,
  `original_image` VARCHAR(255) NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/bsp/TensileTestRecord.java
CREATE TABLE IF NOT EXISTS `tensile_test_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `test_name` VARCHAR(100) NULL,
  `lot_no` VARCHAR(100) NULL,
  `sample_no` VARCHAR(100) NULL,
  `strand_no` VARCHAR(100) NULL,
  `test` VARCHAR(100) NULL,
  `sample_test_dtm` VARCHAR(100) NULL,
  `ys` VARCHAR(100) NULL,
  `uts` VARCHAR(100) NULL,
  `elongation` VARCHAR(100) NULL,
  `hardness` VARCHAR(100) NULL,
  `remarks` VARCHAR(255) NULL,
  `retest_step` VARCHAR(100) NULL,
  `heat_no` VARCHAR(100) NULL,
  `mill` VARCHAR(100) NULL,
  `sms` VARCHAR(100) NULL,
  `rail_grade` VARCHAR(150) NULL,
  `section` VARCHAR(100) NULL,
  `heat_judgment_dt` VARCHAR(100) NULL,
  `heat_status` VARCHAR(150) NULL,
  `heat_judgment_dtm` VARCHAR(100) NULL,
  `created_at` DATE NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/testing/TestingDutyEntity.java
CREATE TABLE IF NOT EXISTS `testing_duty` (
  `duty_id` VARCHAR(50) NOT NULL,
  `user_id` VARCHAR(50) NULL,
  `date` DATE NULL,
  `start_time` DATETIME NULL,
  `end_time` DATETIME NULL,
  `shift` VARCHAR(10) NULL,
  `shift_remarks` VARCHAR(50) NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/visualInspection/ViAcceptDataEntity.java
CREATE TABLE IF NOT EXISTS `visual_inspection_accept_data` (
  `acceptance_id` INT NOT NULL AUTO_INCREMENT,
  `rail_id` VARCHAR(50) NOT NULL,
  `accepted_length` DECIMAL(10, 2) NOT NULL,
  `accepted_number` INT NOT NULL,
  `rail_class` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`acceptance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/visualInspection/ViDefectDataEntity.java
CREATE TABLE IF NOT EXISTS `visual_inspection_defect_data` (
  `defect_data_id` INT NOT NULL AUTO_INCREMENT,
  `defect_category` VARCHAR(50) NOT NULL,
  `defect_type` VARCHAR(50) NOT NULL,
  `location` VARCHAR(50) NOT NULL,
  `position` VARCHAR(50) NOT NULL,
  `rail_id` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`defect_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/visualInspection/ViDutyEntity.java
CREATE TABLE IF NOT EXISTS `visual_inspection_duty` (
  `user_id` VARCHAR(255) NULL,
  `duty_id` VARCHAR(255) NOT NULL,
  `date` DATE NOT NULL,
  `shift` VARCHAR(10) NOT NULL,
  `mill` VARCHAR(10) NOT NULL,
  `line_no` INT NOT NULL,
  `rail_grade` VARCHAR(50) NOT NULL,
  `rail_section` VARCHAR(50) NOT NULL,
  `standard_offered_length` DECIMAL(10, 2) NOT NULL,
  `ie_name_1` VARCHAR(255) NOT NULL,
  `ie_name_2` VARCHAR(255) NULL,
  `ie_name_3` VARCHAR(255) NULL,
  `rcl_ie_name_1` VARCHAR(255) NOT NULL,
  `rcl_ie_name_2` VARCHAR(255) NOT NULL,
  `rcl_ie_name_3` VARCHAR(255) NULL,
  `ie_confirmation` BIT(1) NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NULL,
  `shift_remarks` VARCHAR(100) NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/visualInspection/ViMasterEntity.java
CREATE TABLE IF NOT EXISTS `visual_inspection_master` (
  `heat_number` VARCHAR(50) NOT NULL,
  `rail_id` VARCHAR(50) NOT NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  `date` DATE NOT NULL,
  `shift` VARCHAR(10) NOT NULL,
  `serial_number` VARCHAR(10) NOT NULL,
  `heat_status` VARCHAR(50) NOT NULL,
  `actual_offered_length` DECIMAL(10, 2) NOT NULL,
  `ut_feedback` VARCHAR(100) NULL,
  `dim_feedback` VARCHAR(100) NULL,
  `visual_feedback` VARCHAR(100) NULL,
  `gauging_end_straightness` BIT(1) NULL,
  `rej_13m` INT NULL,
  `rej_12m` INT NULL,
  `rej_11m` INT NULL,
  `rej_10m` INT NULL,
  `rej_comp_length` DECIMAL(5,2) NULL,
  `remarks` VARCHAR(50) NULL,
  `image` LONGBLOB NULL,
  PRIMARY KEY (`rail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/welding/WeldingDutyInspectionRelationEntity.java
CREATE TABLE IF NOT EXISTS `welding_duty_inspection_relation` (
  `duty_id` VARCHAR(50) NOT NULL,
  `inspection_master_id` INT NOT NULL,
  `panel_decision` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`duty_id`, `inspection_master_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/welding/WeldingHardnessEntity.java
CREATE TABLE IF NOT EXISTS `welding_hardness` (
  `test_type` VARCHAR(20) NOT NULL,
  `joint_number` VARCHAR(50) NOT NULL,
  `machine_number` VARCHAR(50) NOT NULL,
  `parent_hardness_l` DECIMAL(10, 2) NOT NULL,
  `parent_hardness_r` DECIMAL(10, 2) NOT NULL,
  `haz_hardness_l` DECIMAL(10, 2) NOT NULL,
  `haz_hardness_r` DECIMAL(10, 2) NOT NULL,
  `deviation_l` DECIMAL(10, 2) NOT NULL,
  `deviation_r` DECIMAL(10, 2) NOT NULL,
  `remark` VARCHAR(50) NOT NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`test_type`, `joint_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/welding/WeldingDetailEntity.java
CREATE TABLE IF NOT EXISTS `welding_inspection_detail` (
  `joint_number` VARCHAR(50) NOT NULL,
  `inspection_master_id` INT NOT NULL,
  `weld_parameter` BIT(1) NULL,
  `marking` BIT(1) NULL,
  `visual` BIT(1) NULL,
  `dimension` BIT(1) NULL,
  `dimension_detail` VARCHAR(50) NULL,
  `usfd` BIT(1) NULL,
  `usfd_detail` VARCHAR(50) NULL,
  `weld_operator` VARCHAR(50) NULL,
  `usfd_operator` VARCHAR(50) NULL,
  `result` VARCHAR(50) NULL,
  `rail_id_1` VARCHAR(50) NOT NULL,
  `rail_id_2` VARCHAR(50) NOT NULL,
  `rail_id_1_length` DECIMAL(10, 2) NOT NULL,
  `rail_id_2_length` DECIMAL(10, 2) NOT NULL,
  `remark` VARCHAR(50) NULL,
  PRIMARY KEY (`joint_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/welding/WeldingInspectionDutyEntity.java
CREATE TABLE IF NOT EXISTS `welding_inspection_duty` (
  `duty_id` VARCHAR(50) NOT NULL,
  `user_id` VARCHAR(50) NOT NULL,
  `date` DATE NOT NULL,
  `shift` VARCHAR(50) NOT NULL,
  `mill` VARCHAR(10) NOT NULL,
  `welding_line` VARCHAR(50) NOT NULL,
  `rail_grade` VARCHAR(50) NOT NULL,
  `rail_section` VARCHAR(50) NOT NULL,
  `shift_remarks` VARCHAR(100) NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NULL,
  `machine_serial_no_head` VARCHAR(50) NULL,
  `machine_serial_no_foot` VARCHAR(50) NULL,
  `probe_details_head` VARCHAR(50) NULL,
  `probe_details_foot` VARCHAR(50) NULL,
  `probe_db_head` VARCHAR(50) NULL,
  `probe_db_foot` VARCHAR(50) NULL,
  `remarks_head` VARCHAR(50) NULL,
  `remarks_foot` VARCHAR(50) NULL,
  `joints_welde_prev_shift_bsp2` INT NULL,
  `joints_welde_prev_shift_bsp1` INT NULL,
  `joints_welde_prev_shift_bsp4` INT NULL,
  PRIMARY KEY (`duty_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/welding/WeldingMasterEntity.java
CREATE TABLE IF NOT EXISTS `welding_inspection_master` (
  `inspection_master_id` INT NOT NULL AUTO_INCREMENT,
  `panel_id` VARCHAR(50) NOT NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  `number_of_joints` INT NOT NULL,
  `panel_length` INT NOT NULL,
  `panel_decision` VARCHAR(50) NOT NULL,
  `panel_remark` VARCHAR(50) NOT NULL,
  `front_result` VARCHAR(50) NULL,
  `back_result` VARCHAR(50) NULL,
  `front_result_reason_for_not_ok` VARCHAR(50) NULL,
  `back_result_reason_for_not_ok` VARCHAR(50) NULL,
  `front_remark` VARCHAR(50) NULL,
  `back_remark` VARCHAR(50) NULL,
  PRIMARY KEY (`inspection_master_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/welding/WeldingMacroEntity.java
CREATE TABLE IF NOT EXISTS `welding_macro` (
  `test_type` VARCHAR(20) NOT NULL,
  `joint_number` VARCHAR(50) NOT NULL,
  `head_l` DECIMAL(10, 2) NULL,
  `web_l` DECIMAL(10, 2) NULL,
  `foot_l` DECIMAL(10, 2) NULL,
  `head_r` DECIMAL(10, 2) NULL,
  `web_r` DECIMAL(10, 2) NULL,
  `foot_r` DECIMAL(10, 2) NULL,
  `remarks` VARCHAR(50) NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  `machine_number` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`test_type`, `joint_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/welding/WeldingMicroEntity.java
CREATE TABLE IF NOT EXISTS `welding_micro` (
  `test_type` VARCHAR(20) NOT NULL,
  `joint_number` VARCHAR(50) NOT NULL,
  `is_micro_free` BIT(1) NULL,
  `is_astm_coarser` BIT(1) NULL,
  `remarks` VARCHAR(50) NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  `machine_number` VARCHAR(50) NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`test_type`, `joint_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/welding/WeldingTestSampleEntity.java
CREATE TABLE IF NOT EXISTS `welding_test_sample` (
  `test_type` VARCHAR(20) NOT NULL,
  `joint_number` VARCHAR(50) NOT NULL,
  `rail_grade` VARCHAR(50) NOT NULL,
  `rail_section` VARCHAR(50) NOT NULL,
  `plant` VARCHAR(20) NOT NULL,
  `status` BIT(1) NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`test_type`, `joint_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: entity/welding/WeldingTltEntity.java
CREATE TABLE IF NOT EXISTS `welding_tlt` (
  `test_type` VARCHAR(20) NOT NULL,
  `joint_number` VARCHAR(50) NOT NULL,
  `machine_number` VARCHAR(50) NOT NULL,
  `span` DECIMAL(10, 2) NOT NULL,
  `load_applied` DECIMAL(10, 2) NOT NULL,
  `deflection` DECIMAL(10, 2) NULL,
  `load_at_20_mm_deflection` DECIMAL(10, 2) NOT NULL,
  `is_broken` BIT(1) NULL,
  `remark` VARCHAR(50) NOT NULL,
  `duty_id` VARCHAR(50) NOT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`test_type`, `joint_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: dto/rolling/TestSampleAcptEntity.java
CREATE TABLE IF NOT EXISTS `rolling_test_sample_acceptance` (
  `rolling_test_sample_id` INT NOT NULL AUTO_INCREMENT,
  `rail_grade` VARCHAR(50) NULL,
  `sample_type` VARCHAR(50) NULL,
  `sample_id` VARCHAR(50) NULL,
  `heat_number` VARCHAR(50) NULL,
  `strand` INT NULL,
  `sample_lot` VARCHAR(50) NULL,
  `chemical` BOOLEAN DEFAULT FALSE NULL,
  `n2` BOOLEAN DEFAULT FALSE NULL,
  `fwt_st` BOOLEAN DEFAULT FALSE NULL,
  `mechanical` BOOLEAN DEFAULT FALSE NULL,
  `sp` BOOLEAN DEFAULT FALSE NULL,
  `ir` BOOLEAN DEFAULT FALSE NULL,
  `o2` BOOLEAN DEFAULT FALSE NULL,
  `fwt_hs` BOOLEAN DEFAULT FALSE NULL,
  `fwt_st_sr` BOOLEAN DEFAULT FALSE NULL,
  `tensile_foot` BOOLEAN DEFAULT FALSE NULL,
  `micro` BOOLEAN DEFAULT FALSE NULL,
  `decarb` BOOLEAN DEFAULT FALSE NULL,
  `rsh` BOOLEAN DEFAULT FALSE NULL,
  `ph` BOOLEAN DEFAULT FALSE NULL,
  `tensile` BOOLEAN DEFAULT FALSE NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`rolling_test_sample_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- entity: dto/vi/ViSubRailIdEntity.java
CREATE TABLE IF NOT EXISTS `visual_inspection_sub_rail_id` (
  `rail_id` VARCHAR(50) NOT NULL,
  `sub_rail_id` VARCHAR(50) NOT NULL,
  `accepted_length` DECIMAL(10,2) NULL,
  PRIMARY KEY (`rail_id`, `sub_rail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- VERIFY (read only): expect 58
SELECT COUNT(*) AS railmill_tables_present
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN (
    'QCT_MASTER',
    'calibration_detail',
    'calibration_duty',
    'chem_test_record',
    'completed_tests',
    'dimensional_inspection',
    'fwt_test_record',
    'heatnumber_inspection',
    'ht_sequence',
    'ht_sequence_heat',
    'ht_sequence_heat_rail',
    'macro_test_record',
    'ndt_calibration',
    'ndt_duty',
    'ndt_duty_clb_prev_data',
    'ndt_shift_summary',
    'qct_center_line_master',
    'qct_detail',
    'qct_duty',
    'qct_fatigue_detail',
    'qct_fatigue_master',
    'qct_fcgr_master',
    'qct_fracture_toughness_master',
    'qct_residual_master',
    'qct_testing_detail_table',
    'qct_testing_master_table',
    'retest_sample',
    'rolling_control_heat_detail',
    'rolling_duty',
    'rolling_finishing_verification_hot_stamping',
    'rolling_finishing_verification_pre_cambering',
    'rolling_finishing_verification_straightening_machine',
    'rolling_temperature_observation',
    'rolling_test_sample_acceptance_relation',
    'rolling_verification_bloom_yard_detail',
    'rolling_verification_charging_detail',
    'short_rail_duty',
    'short_rail_master',
    'short_rail_rejection_analysis',
    'sms_shift_summary_master',
    'surface_inspection',
    'tensile_test_record',
    'testing_duty',
    'visual_inspection_accept_data',
    'visual_inspection_defect_data',
    'visual_inspection_duty',
    'visual_inspection_master',
    'welding_duty_inspection_relation',
    'welding_hardness',
    'welding_inspection_detail',
    'welding_inspection_duty',
    'welding_inspection_master',
    'welding_macro',
    'welding_micro',
    'welding_test_sample',
    'welding_tlt',
    'rolling_test_sample_acceptance',
    'visual_inspection_sub_rail_id'
  );
