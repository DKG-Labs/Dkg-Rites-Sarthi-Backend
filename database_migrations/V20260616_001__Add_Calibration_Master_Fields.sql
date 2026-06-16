-- SQL Migration: Add Master Fields to Vendor Calibration Tables

ALTER TABLE `vendor_calibration_details`
    ADD COLUMN `make_model` VARCHAR(150) DEFAULT NULL,
    ADD COLUMN `master_equip_no_cert_validity` VARCHAR(255) DEFAULT NULL,
    ADD COLUMN `master_equip_nabl_details` VARCHAR(255) DEFAULT NULL;

ALTER TABLE `ie_vendor_calibration_inspection_detail`
    ADD COLUMN `make_model` VARCHAR(150) DEFAULT NULL,
    ADD COLUMN `master_equip_no_cert_validity` VARCHAR(255) DEFAULT NULL,
    ADD COLUMN `master_equip_nabl_details` VARCHAR(255) DEFAULT NULL;
