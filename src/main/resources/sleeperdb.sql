
CREATE TABLE mould_preparation (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    line_shed_no VARCHAR(50),

    preparation_date DATE,

    preparation_time TIME,

    batch_no VARCHAR(50),

    bench_no VARCHAR(50),

    mould_cleaned BOOLEAN,

    oil_applied BOOLEAN,

    remarks VARCHAR(500),

    created_by int,

    updated_by int,

    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    status varchar(20)

);

select *from hts_wire_placement


CREATE TABLE hts_wire_placement (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    line_shed_no VARCHAR(50),

    placement_date DATE,

    placement_time VARCHAR(10),

    batch_no VARCHAR(50),

    bench_no VARCHAR(50),

    sleeper_type VARCHAR(50),

    no_of_wires_used INT,

    hts_wire_dia_mm DECIMAL(6,2),

    lay_length_mm DECIMAL(8,2),

    arrangement_ok BOOLEAN,

    overall_status VARCHAR(20),

    remarks VARCHAR(500),

    created_by int,

    updated_by int,

    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    status varchar(50)

);



CREATE TABLE demoulding_inspection (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    line_shed_no VARCHAR(50),

    inspection_date DATE,

    inspection_time VARCHAR(20),

    casting_date DATE,

    batch_no VARCHAR(50),

    bench_no VARCHAR(50),

    sleeper_type VARCHAR(50),

    process_status VARCHAR(50),

    visual_check VARCHAR(50),

    dim_check VARCHAR(50),

    overall_remarks VARCHAR(500),

    created_by VARCHAR(100),

    updated_by VARCHAR(100),

    created_date TIMESTAMP,

    updated_date TIMESTAMP,

    status varchar(20)

);

CREATE TABLE demoulding_defective_sleepers (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    inspection_id BIGINT,

    bench_gang_no VARCHAR(50),

    sequence_no VARCHAR(50),

    sleeper_no VARCHAR(50),

    visual_reason VARCHAR(100),

    dim_reason VARCHAR(100),

    created_date TIMESTAMP,

    FOREIGN KEY (inspection_id)
        REFERENCES demoulding_inspection(id)
);

drop table  moisture_analysis_entry
CREATE TABLE moisture_analysis_entry (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    /* Common Form Section */

    entry_date DATE,

    shift VARCHAR(20),

    entry_time VARCHAR(20),

    batch_no VARCHAR(50),

    batch_wt_dry_ca1 DECIMAL(10,2),

    batch_wt_dry_ca2 DECIMAL(10,2),

    batch_wt_dry_fa DECIMAL(10,2),

    batch_wt_dry_water DECIMAL(10,2),

    batch_wt_dry_admix DECIMAL(10,2),

    batch_wt_dry_cement DECIMAL(10,2),

    wt_adopted_ca1 DECIMAL(10,2),

    wt_adopted_ca2 DECIMAL(10,2),

    wt_adopted_fa DECIMAL(10,2),

    total_free_moisture DECIMAL(10,2),

    adjusted_water_wt DECIMAL(10,2),

    wc_ratio DECIMAL(10,2),

    ac_ratio DECIMAL(10,2),


    /* Section Info */

    section_type VARCHAR(10),
    -- CA1 / CA2 / FA


    /* Section Fields (Used based on section_type) */

    wt_wet_sample DECIMAL(10,2),

    wt_dried_sample DECIMAL(10,2),

    wt_moisture_sample DECIMAL(10,2),

    moisture_percent DECIMAL(10,2),

    absorption_percent DECIMAL(10,2),

    free_moisture_percent DECIMAL(10,2),

    batch_wt_dry DECIMAL(10,2),

    free_moisture_kg DECIMAL(10,2),

    adjusted_weight DECIMAL(10,2),

    adopted_weight DECIMAL(10,2),


    /* Audit */

    created_by VARCHAR(100),

    updated_by VARCHAR(100),

    created_date TIMESTAMP,

    updated_date TIMESTAMP,

    status varchar(40)

);



CREATE TABLE bench_mould_inspection (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    line_shed_no VARCHAR(50),

    checking_date DATE,

    bench_gang_no VARCHAR(50),

    sleeper_type VARCHAR(50),

    latest_casting_date DATE,

    bench_visual_result VARCHAR(100),
    bench_dimensional_result VARCHAR(100),

    mould_visual_result VARCHAR(100),
    mould_dimensional_result VARCHAR(100),

    combined_remarks TEXT,

    created_by VARCHAR(50),
    updated_by VARCHAR(50),

    created_date DATETIME,
    updated_date DATETIME,

    status VARCHAR(10)
);



CREATE TABLE production_declaration (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    plant_type VARCHAR(50),
    production_unit VARCHAR(100),
    casting_date DATE,
    shift VARCHAR(50),

    batch_number VARCHAR(50),
    mix_design_reference VARCHAR(100),
    lbc_time TIME,

    total_casted_sleepers INT,
    total_sleeper_types INT,
    total_rft_casted DOUBLE,

    remarks VARCHAR(255),

    created_by INT,
    updated_by INT,
    created_date DATETIME,
    updated_date DATETIME
);


CREATE TABLE production_chamber (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    declaration_id BIGINT,

    chamber_no VARCHAR(50),

    created_by INT,
    updated_by INT,
    created_date DATETIME,
    updated_date DATETIME,

    CONSTRAINT fk_prod_chamber_decl
    FOREIGN KEY (declaration_id)
    REFERENCES production_declaration(id)
);


CREATE TABLE production_bench (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    chamber_id BIGINT,

    bench_no VARCHAR(50),
    count int,
    sleeper_type VARCHAR(100),
    mould_per_bench INT,
    rft_meters DOUBLE,

    created_by INT,
    updated_by INT,
    created_date DATETIME,
    updated_date DATETIME,

    CONSTRAINT fk_prod_bench_chamber
    FOREIGN KEY (chamber_id)
    REFERENCES production_chamber(id)
);


CREATE TABLE hts_wire (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    date_of_receipt DATE,
    grade_spec VARCHAR(100),
    manufacturer VARCHAR(150),

    invoice_number VARCHAR(100),
    invoice_date DATE,

    rites_ic_number VARCHAR(100),
    rites_ic_date DATE,

    relaxation_test VARCHAR(10),
    relaxation_test_date DATE,

    total_qty_received DOUBLE,

    created_by INT,
    updated_by INT,
    created_date DATETIME,
    updated_date DATETIME
);

drop table  hts_coil_details

select *from  hts_coil_details
CREATE TABLE hts_coil_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    coil_from VARCHAR(50),
    coil_to VARCHAR(50),

    lot_no VARCHAR(50),
	coil_no VARCHAR(50),
    qty_kg DOUBLE,

    entry_type VARCHAR(10),

    hts_wire_id BIGINT,

    CONSTRAINT fk_hts_wire
        FOREIGN KEY (hts_wire_id)
        REFERENCES hts_wire(id)
        ON DELETE CASCADE
);


CREATE TABLE cement_receipt (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    date_of_receipt DATE,
    grade_spec VARCHAR(100),
    manufacturer VARCHAR(150),

    invoice_number VARCHAR(100),
    invoice_date DATE,

    total_qty_received DOUBLE,

    created_by INT,
    created_date DATETIME,

    updated_by INT,
    updated_date DATETIME
);


CREATE TABLE cement_batch_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    week_no INT,
    year_no INT,
    mtc_no VARCHAR(100),
    quantity_kg DOUBLE,

    cement_receipt_id BIGINT,

    CONSTRAINT fk_cement_receipt
        FOREIGN KEY (cement_receipt_id)
        REFERENCES cement_receipt(id)
        ON DELETE CASCADE
);

CREATE TABLE admixture_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    date_of_receipt DATE,
    manufacturer VARCHAR(150),
    grade_spec VARCHAR(100),

    invoice_number VARCHAR(100),
    invoice_date DATE,

    lot_no VARCHAR(100),
    mtc_no VARCHAR(100),

    total_quantity DOUBLE,

    created_by INT,
    created_date DATETIME,

    updated_by INT,
    updated_date DATETIME
);

CREATE TABLE aggregates_inventory (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    date_of_receipt DATE,
    grade_spec VARCHAR(100),
    source VARCHAR(150),

    challan_number VARCHAR(100),
    challan_date DATE,

    total_qty_received DOUBLE,

    created_by INT,
    created_date DATETIME,

    updated_by INT,
    updated_date DATETIME
);

CREATE TABLE sgci_insert_inventory (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    date_of_receipt DATE,

    grade_type VARCHAR(150),
    manufacturer VARCHAR(150),

    invoice_number VARCHAR(100),
    invoice_date DATE,

    rites_ic_number VARCHAR(100),
    rites_ic_date DATE,

    total_qty_received INT,

    created_by INT,
    created_date DATETIME,

    updated_by INT,
    updated_date DATETIME
);

CREATE TABLE dowel_inventory (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    date_of_receipt DATE,

    grade_type VARCHAR(150),
    manufacturer VARCHAR(150),

    invoice_number VARCHAR(100),
    invoice_date DATE,

    rites_ic_number VARCHAR(100),
    rites_ic_date DATE,

    total_qty_received INT,

    created_by INT,
    created_date DATETIME,

    updated_by INT,
    updated_date DATETIME
);

CREATE TABLE plant_profile (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    plant_name_location VARCHAR(255),
    vendor_code VARCHAR(100),
    plant_type VARCHAR(100),
    number_of_sheds INT,

    created_by INT,
    created_date DATETIME,

    updated_by INT,
    updated_date DATETIME
);

CREATE TABLE raw_material_source (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    raw_material_type VARCHAR(150),
    supplier_name VARCHAR(200),
    approval_reference VARCHAR(200),

    valid_from DATE,
    valid_to DATE,

    created_by INT,
    created_date DATETIME,

    updated_by INT,
    updated_date DATETIME
);


CREATE TABLE mix_design (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    identification VARCHAR(150),
    concrete_grade VARCHAR(100),
    authority_of_approval VARCHAR(100),

    cement DOUBLE,
    ca1 DOUBLE,
    ca2 DOUBLE,
    fa DOUBLE,
    water DOUBLE,

    ac_ratio DOUBLE,
    wc_ratio DOUBLE,

    ca1_proportion DOUBLE DEFAULT NULL,
    ca2_proportion DOUBLE DEFAULT NULL,
    fa_proportion DOUBLE DEFAULT NULL,
    grading_range_lower DOUBLE DEFAULT NULL,
    grading_range_upper DOUBLE DEFAULT NULL,

    created_by INT,
    created_date DATETIME,

    updated_by INT,
    updated_date DATETIME
);


CREATE TABLE stress_bench_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    bench_no INT NULL,
    bench_from INT NULL,
    bench_to INT NULL,

    no_of_benches INT,

    sleeper_category VARCHAR(50),

    moulds_per_bench INT,

    entry_type VARCHAR(20),   -- RANGE or SINGLE

    created_by BIGINT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_by BIGINT,
    updated_date TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
);



CREATE TABLE sleeper_workflow_transaction (
    workflow_transition_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workflow_id BIGINT,
    module_id BIGINT,
    request_id VARCHAR(255),
    action VARCHAR(100),
    status VARCHAR(100),
    remarks TEXT,
    assigned_to_user BIGINT,
    created_by BIGINT,
    modified_by BIGINT,
    created_date DATETIME,
    updated_date DATETIME
);

CREATE TABLE sleeper_workflow (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workflow_name VARCHAR(255) NOT NULL,
    created_by BIGINT,
    created_date DATETIME
);

CREATE TABLE sleeper_module (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    module_name VARCHAR(255) NOT NULL,
    workflow_id BIGINT,
    created_by BIGINT,
    created_date DATETIME,
    FOREIGN KEY (workflow_id) REFERENCES sleeper_workflow(id)
);




////////////////////////////
/////////////////////
//////
CREATE TABLE `plant_profile` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `plant_name_location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `vendor_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `plant_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `number_of_sheds` int DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `plant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 )

CREATE TABLE `bm_master` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `plant_type` varchar(50) NOT NULL,
   `category` varchar(100) DEFAULT NULL,
   `sub_category` varchar(150) DEFAULT NULL,
   `drawing_no` varchar(50) DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
   `updated_by` int DEFAULT NULL,
   `updated_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   `vendor_code` varchar(50) DEFAULT NULL,
   `plant_id` varchar(50) DEFAULT NULL,
   PRIMARY KEY (`id`)
 )
CREATE TABLE `bm_longline_details` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `bm_master_id` bigint NOT NULL,
   `sleeper_code` varchar(50) DEFAULT NULL,
   `sleeper_drawing_no` varchar(50) DEFAULT NULL,
   `declaration_mode` varchar(20) DEFAULT NULL,
   `gang_from` int DEFAULT NULL,
   `gang_to` int DEFAULT NULL,
   `gang_number` int DEFAULT NULL,
   `no_of_moulds` int NOT NULL,
   `created_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
   `updated_by` int DEFAULT NULL,
   `updated_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   PRIMARY KEY (`id`),
   KEY `fk_longline_master` (`bm_master_id`),
   CONSTRAINT `fk_longline_master` FOREIGN KEY (`bm_master_id`) REFERENCES `bm_master` (`id`) ON DELETE CASCADE
 )

 CREATE TABLE `bm_stress_details` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `bm_master_id` bigint NOT NULL,
    `sleeper_code` varchar(50) DEFAULT NULL,
    `sleeper_drawing_no` varchar(50) DEFAULT NULL,
    `declaration_mode` varchar(20) DEFAULT NULL,
    `bench_from` int DEFAULT NULL,
    `bench_to` int DEFAULT NULL,
    `bench_number` int DEFAULT NULL,
    `no_of_moulds` int NOT NULL,
    `created_by` int DEFAULT NULL,
    `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
    `updated_by` int DEFAULT NULL,
    `updated_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `fk_stress_master` (`bm_master_id`),
    CONSTRAINT `fk_stress_master` FOREIGN KEY (`bm_master_id`) REFERENCES `bm_master` (`id`) ON DELETE CASCADE
  )

  CREATE TABLE `raw_material_source` (
     `id` bigint NOT NULL AUTO_INCREMENT,
     `raw_material_type` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
     `supplier_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
     `approval_reference` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
     `valid_from` date DEFAULT NULL,
     `valid_to` date DEFAULT NULL,
     `created_by` int DEFAULT NULL,
     `created_date` datetime DEFAULT NULL,
     `updated_by` int DEFAULT NULL,
     `updated_date` datetime DEFAULT NULL,
     `vendor_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
     `plant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
     PRIMARY KEY (`id`)
   )

   CREATE TABLE `mix_design` (
      `id` bigint NOT NULL AUTO_INCREMENT,
      `identification` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `concrete_grade` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `authority_of_approval` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `cement` double DEFAULT NULL,
      `ca1` double DEFAULT NULL,
      `ca2` double DEFAULT NULL,
      `fa` double DEFAULT NULL,
      `water` double DEFAULT NULL,
      `ac_ratio` double DEFAULT NULL,
      `wc_ratio` double DEFAULT NULL,
      `ca1_proportion` double DEFAULT NULL,
      `ca2_proportion` double DEFAULT NULL,
      `fa_proportion` double DEFAULT NULL,
      `grading_range_lower` double DEFAULT NULL,
      `grading_range_upper` double DEFAULT NULL,
      `created_by` int DEFAULT NULL,
      `created_date` datetime DEFAULT NULL,
      `updated_by` int DEFAULT NULL,
      `updated_date` datetime DEFAULT NULL,
      `vendor_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      `plant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
      PRIMARY KEY (`id`)
    )

CREATE TABLE `hts_wire` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `date_of_receipt` date DEFAULT NULL,
   `grade_spec` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `manufacturer` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_date` date DEFAULT NULL,
   `rites_ic_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `rites_ic_date` date DEFAULT NULL,
   `relaxation_test` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `relaxation_test_date` date DEFAULT NULL,
   `total_qty_received` double DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `vendor_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `plant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 )

 CREATE TABLE `hts_coil_details` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `coil_from` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `coil_to` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `lot_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `coil_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `qty_kg` double DEFAULT NULL,
    `entry_type` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `hts_wire_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_hts_wire` (`hts_wire_id`),
    CONSTRAINT `fk_hts_wire` FOREIGN KEY (`hts_wire_id`) REFERENCES `hts_wire` (`id`) ON DELETE CASCADE
  )

CREATE TABLE `cement_receipt` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `date_of_receipt` date DEFAULT NULL,
   `grade_spec` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `manufacturer` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_date` date DEFAULT NULL,
   `total_qty_received` double DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `vendor_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `plant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 )
 CREATE TABLE `cement_batch_details` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `week_no` int DEFAULT NULL,
    `year_no` int DEFAULT NULL,
    `mtc_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `quantity_kg` double DEFAULT NULL,
    `cement_receipt_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_cement_receipt` (`cement_receipt_id`),
    CONSTRAINT `fk_cement_receipt` FOREIGN KEY (`cement_receipt_id`) REFERENCES `cement_receipt` (`id`) ON DELETE CASCADE
  )

CREATE TABLE `aggregates_inventory` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `date_of_receipt` date DEFAULT NULL,
   `grade_spec` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `source` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `challan_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `challan_date` date DEFAULT NULL,
   `total_qty_received` double DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `vendor_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `plant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 )

CREATE TABLE `admixture_inventory` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `date_of_receipt` date DEFAULT NULL,
   `manufacturer` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `grade_spec` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_date` date DEFAULT NULL,
   `lot_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `mtc_no` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `total_quantity` double DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `vendor_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `plant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 );

CREATE TABLE `sgci_insert_inventory` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `date_of_receipt` date DEFAULT NULL,
   `grade_type` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `manufacturer` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_date` date DEFAULT NULL,
   `rites_ic_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `rites_ic_date` date DEFAULT NULL,
   `total_qty_received` int DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `vendor_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `plant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 );

CREATE TABLE `dowel_inventory` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `date_of_receipt` date DEFAULT NULL,
   `grade_type` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `manufacturer` varchar(150) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `invoice_date` date DEFAULT NULL,
   `rites_ic_number` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `rites_ic_date` date DEFAULT NULL,
   `total_qty_received` int DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `vendor_code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `plant_id` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 );
CREATE TABLE `production_declaration` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `plant_type` varchar(30) DEFAULT NULL,
   `production_unit` varchar(100) DEFAULT NULL,
   `casting_date` date DEFAULT NULL,
   `shift` varchar(50) DEFAULT NULL,
   `batch_number` varchar(50) DEFAULT NULL,
   `mix_design_reference` varchar(100) DEFAULT NULL,
   `lbc_time` time DEFAULT NULL,
   `total_casted_sleepers` int DEFAULT NULL,
   `total_sleeper_types` int DEFAULT NULL,
   `total_rft` double DEFAULT NULL,
   `remarks` text,
   `created_by` bigint DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_by` bigint DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `vendor_code` varchar(50) DEFAULT NULL,
   `plant_id` varchar(50) DEFAULT NULL,
   PRIMARY KEY (`id`)
 )
CREATE TABLE `production_stress_chamber` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `declaration_id` bigint DEFAULT NULL,
   `chamber_no` int DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `declaration_id` (`declaration_id`),
   CONSTRAINT `production_stress_chamber_ibfk_1` FOREIGN KEY (`declaration_id`) REFERENCES `production_declaration` (`id`) ON DELETE CASCADE
 )

CREATE TABLE `production_bench_group` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `chamber_id` bigint DEFAULT NULL,
   `bench_no` int DEFAULT NULL,
   `sleeper_type` varchar(100) DEFAULT NULL,
   `mould_per_bench` int DEFAULT NULL,
   `rft` double DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `chamber_id` (`chamber_id`),
   CONSTRAINT `production_bench_group_ibfk_1` FOREIGN KEY (`chamber_id`) REFERENCES `production_stress_chamber` (`id`) ON DELETE CASCADE
 );

CREATE TABLE `production_longline_gang` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `declaration_id` bigint DEFAULT NULL,
   `mode` varchar(20) DEFAULT NULL,
   `gang_from` int DEFAULT NULL,
   `gang_to` int DEFAULT NULL,
   `gang_no` int DEFAULT NULL,
   `sleeper_type` varchar(100) DEFAULT NULL,
   `moulds_per_gang` int DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `declaration_id` (`declaration_id`),
   CONSTRAINT `production_longline_gang_ibfk_1` FOREIGN KEY (`declaration_id`) REFERENCES `production_declaration` (`id`) ON DELETE CASCADE
 );

    CREATE TABLE `production_sleeper` (
       `id` bigint NOT NULL AUTO_INCREMENT,
       `bench_group_id` bigint DEFAULT NULL,
       `sleeper_no` varchar(20) DEFAULT NULL,
       `gang_id` bigint DEFAULT NULL,
       PRIMARY KEY (`id`),
       KEY `bench_group_id` (`bench_group_id`),
       CONSTRAINT `production_sleeper_ibfk_1` FOREIGN KEY (`bench_group_id`) REFERENCES `production_bench_group` (`id`) ON DELETE CASCADE
     );

CREATE TABLE `inspection_module` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `module_name` varchar(100) DEFAULT NULL,
   PRIMARY KEY (`id`)
 );---->


CREATE TABLE `inspection_test_header` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `batch_id` bigint DEFAULT NULL,
   `sleeper_type` varchar(100) DEFAULT NULL,
   `shift` varchar(20) DEFAULT NULL,
   `test_date` date DEFAULT NULL,
   `created_by` bigint DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `module_id` bigint DEFAULT NULL,
   `status` varchar(30) DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_test_header_module` (`module_id`),
   CONSTRAINT `fk_test_header_module` FOREIGN KEY (`module_id`) REFERENCES `inspection_module` (`id`)
 );

CREATE TABLE `inspection_parameter` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `parameter_name` varchar(200) DEFAULT NULL,
   `module_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_parameter_module` (`module_id`),
   CONSTRAINT `fk_parameter_module` FOREIGN KEY (`module_id`) REFERENCES `inspection_module` (`id`)
)  --->


CREATE TABLE `inspection_parameter_result` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `parameter_result` varchar(20) DEFAULT NULL,
   `parameter_id` bigint DEFAULT NULL,
   `test_result_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_parameter_result_parameter` (`parameter_id`),
   KEY `fk_parameter_result_test` (`test_result_id`),
   CONSTRAINT `fk_parameter_result_parameter` FOREIGN KEY (`parameter_id`) REFERENCES `inspection_parameter` (`id`),
   CONSTRAINT `fk_parameter_result_test` FOREIGN KEY (`test_result_id`) REFERENCES `inspection_test_result` (`id`)
 )

CREATE TABLE `inspection_test_result` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `sleeper_id` bigint DEFAULT NULL,
   `sleeper_no` varchar(50) DEFAULT NULL,
   `result` varchar(20) DEFAULT NULL,
   `rejection_reason` varchar(255) DEFAULT NULL,
   `test_header_id` bigint DEFAULT NULL,
   `module_id` bigint DEFAULT NULL,
   `active` tinyint(1) DEFAULT '1',
   `updated_date` datetime DEFAULT NULL,
   `updated_by` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_test_result_header` (`test_header_id`),
   CONSTRAINT `fk_test_result_header` FOREIGN KEY (`test_header_id`) REFERENCES `inspection_test_header` (`id`)
 );

CREATE TABLE `mould_preparation` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `line_shed_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `preparation_date` date DEFAULT NULL,
   `preparation_time` time DEFAULT NULL,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `bench_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `mould_cleaned` tinyint(1) DEFAULT NULL,
   `oil_applied` tinyint(1) DEFAULT NULL,
   `remarks` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
   `updated_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 );

CREATE TABLE `hts_wire_placement` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `line_shed_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `placement_date` date DEFAULT NULL,
   `placement_time` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `bench_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `sleeper_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `no_of_wires_used` int DEFAULT NULL,
   `hts_wire_dia_mm` decimal(6,2) DEFAULT NULL,
   `lay_length_mm` decimal(8,2) DEFAULT NULL,
   `arrangement_ok` tinyint(1) DEFAULT NULL,
   `overall_status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `remarks` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `created_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
   `updated_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   `status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 );

CREATE TABLE `demoulding_inspection` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `line_shed_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `inspection_date` date DEFAULT NULL,
   `inspection_time` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `casting_date` date DEFAULT NULL,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `bench_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `sleeper_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `process_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `visual_check` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `dim_check` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `overall_remarks` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `created_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `updated_by` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `created_date` timestamp NULL DEFAULT NULL,
   `updated_date` timestamp NULL DEFAULT NULL,
   `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 );


CREATE TABLE `demoulding_defective_sleepers` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `inspection_id` bigint DEFAULT NULL,
   `bench_gang_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `sequence_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `sleeper_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `visual_reason` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `dim_reason` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `created_date` timestamp NULL DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `inspection_id` (`inspection_id`),
   CONSTRAINT `demoulding_defective_sleepers_ibfk_1` FOREIGN KEY (`inspection_id`) REFERENCES `demoulding_inspection` (`id`)
 );


CREATE TABLE `moisture_analysis_entry` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `entry_date` date DEFAULT NULL,
   `shift` varchar(10) DEFAULT NULL,
   `entry_time` varchar(20) DEFAULT NULL,
   `batch_no` varchar(50) DEFAULT NULL,
   `approved_mix_design` varchar(100) DEFAULT NULL,
   `design_ac` double DEFAULT NULL,
   `design_wc` double DEFAULT NULL,
   `design_cement` double DEFAULT NULL,
   `design_ca1` double DEFAULT NULL,
   `design_ca2` double DEFAULT NULL,
   `design_fa` double DEFAULT NULL,
   `design_water` double DEFAULT NULL,
   `design_admix` double DEFAULT NULL,
   `actual_cement` double DEFAULT NULL,
   `actual_ca1` double DEFAULT NULL,
   `actual_ca2` double DEFAULT NULL,
   `actual_fa` double DEFAULT NULL,
   `actual_water` double DEFAULT NULL,
   `actual_admix` double DEFAULT NULL,
   `wt_adopted_ca1` double DEFAULT NULL,
   `wt_adopted_ca2` double DEFAULT NULL,
   `wt_adopted_fa` double DEFAULT NULL,
   `total_free_moisture` double DEFAULT NULL,
   `adjusted_water_wt` double DEFAULT NULL,
   `wc_ratio` double DEFAULT NULL,
   `ac_ratio` double DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `status` varchar(20) DEFAULT NULL,
   PRIMARY KEY (`id`)
 );

 CREATE TABLE `moisture_section` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `section_type` varchar(10) DEFAULT NULL,
    `wt_wet_sample` double DEFAULT NULL,
    `wt_dried_sample` double DEFAULT NULL,
    `wt_moisture_sample` double DEFAULT NULL,
    `moisture_percent` double DEFAULT NULL,
    `absorption_percent` double DEFAULT NULL,
    `free_moisture_percent` double DEFAULT NULL,
    `batch_wt_dry` double DEFAULT NULL,
    `free_moisture_kg` double DEFAULT NULL,
    `adjusted_weight` double DEFAULT NULL,
    `adopted_weight` double DEFAULT NULL,
    `entry_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_moisture_entry` (`entry_id`),
    CONSTRAINT `fk_moisture_entry` FOREIGN KEY (`entry_id`) REFERENCES `moisture_analysis_entry` (`id`) ON DELETE CASCADE
  );

CREATE TABLE `batch_weighment` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `line_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `entry_date` date DEFAULT NULL,
   `sand_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `moisture_sensor_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `verified_by` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `remarks` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `entry_mode` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `created_date` timestamp NULL DEFAULT NULL,
   `updated_date` timestamp NULL DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   PRIMARY KEY (`id`)
 );


CREATE TABLE `batch_details` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `proportion_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `ca1_ref` double DEFAULT NULL,
   `ca2_ref` double DEFAULT NULL,
   `fa_ref` double DEFAULT NULL,
   `cement_ref` double DEFAULT NULL,
   `water_ref` double DEFAULT NULL,
   `admixture_ref` double DEFAULT NULL,
   `ca1_set` double DEFAULT NULL,
   `ca2_set` double DEFAULT NULL,
   `fa_set` double DEFAULT NULL,
   `cement_set` double DEFAULT NULL,
   `water_set` double DEFAULT NULL,
   `admixture_set` double DEFAULT NULL,
   `batch_weighment_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_batch_details_weighment` (`batch_weighment_id`),
   CONSTRAINT `fk_batch_details_weighment` FOREIGN KEY (`batch_weighment_id`) REFERENCES `batch_weighment` (`id`)
 );

 CREATE TABLE `scada_weighment` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `date` date DEFAULT NULL,
    `time` time DEFAULT NULL,
    `ca1_set` double DEFAULT NULL,
    `ca1_actual` double DEFAULT NULL,
    `ca2_set` double DEFAULT NULL,
    `ca2_actual` double DEFAULT NULL,
    `fa_set` double DEFAULT NULL,
    `fa_actual` double DEFAULT NULL,
    `cement_set` double DEFAULT NULL,
    `cement_actual` double DEFAULT NULL,
    `water_set` double DEFAULT NULL,
    `water_actual` double DEFAULT NULL,
    `admixture_set` double DEFAULT NULL,
    `admixture_actual` double DEFAULT NULL,
    `total` double DEFAULT NULL,
    `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `batch_weighment_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_scada_weighment_batch` (`batch_weighment_id`),
    CONSTRAINT `fk_scada_weighment_batch` FOREIGN KEY (`batch_weighment_id`) REFERENCES `batch_weighment` (`id`)
  );

CREATE TABLE `manual_weighment` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `date` date DEFAULT NULL,
   `time` time DEFAULT NULL,
   `ca1_actual` double DEFAULT NULL,
   `ca2_actual` double DEFAULT NULL,
   `fa_actual` double DEFAULT NULL,
   `cement_actual` double DEFAULT NULL,
   `water_actual` double DEFAULT NULL,
   `admixture_actual` double DEFAULT NULL,
   `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `batch_weighment_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_manual_weighment_batch` (`batch_weighment_id`),
   CONSTRAINT `fk_manual_weighment_batch` FOREIGN KEY (`batch_weighment_id`) REFERENCES `batch_weighment` (`id`)
 );

CREATE TABLE `wire_tensioning` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `sleeper_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `wires_per_sleeper` int DEFAULT NULL,
   `target_load_kn` double DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `created_date` timestamp NULL DEFAULT NULL,
   `updated_date` timestamp NULL DEFAULT NULL,
   PRIMARY KEY (`id`)
 );


 CREATE TABLE `wire_tensioning_scada` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `plc_time` time DEFAULT NULL,
    `bench_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `wire_length` double DEFAULT NULL,
    `cross_section` double DEFAULT NULL,
    `youngs_modulus` double DEFAULT NULL,
    `measured_elongation` double DEFAULT NULL,
    `force_elongation` double DEFAULT NULL,
    `total_load` double DEFAULT NULL,
    `final_load` double DEFAULT NULL,
    `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `wire_tensioning_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_wire_scada` (`wire_tensioning_id`),
    CONSTRAINT `fk_wire_scada` FOREIGN KEY (`wire_tensioning_id`) REFERENCES `wire_tensioning` (`id`)
  );


CREATE TABLE `wire_tensioning_manual` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `bench_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `time` time DEFAULT NULL,
   `wire_length` double DEFAULT NULL,
   `cross_section` double DEFAULT NULL,
   `youngs_modulus` double DEFAULT NULL,
   `measured_elongation` double DEFAULT NULL,
   `force_elongation` double DEFAULT NULL,
   `total_load` double DEFAULT NULL,
   `final_load` double DEFAULT NULL,
   `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `wire_tensioning_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_wire_manual` (`wire_tensioning_id`),
   CONSTRAINT `fk_wire_manual` FOREIGN KEY (`wire_tensioning_id`) REFERENCES `wire_tensioning` (`id`)
 ) ;


CREATE TABLE `compaction` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `sleeper_type` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `entry_date` date DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `created_date` timestamp NULL DEFAULT NULL,
   `updated_date` timestamp NULL DEFAULT NULL,
   PRIMARY KEY (`id`)
 );


CREATE TABLE `compaction_scada` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `time` time DEFAULT NULL,
   `bench_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `v1_v4_rpm` double DEFAULT NULL,
   `duration` double DEFAULT NULL,
   `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `compaction_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_compaction_scada` (`compaction_id`),
   CONSTRAINT `fk_compaction_scada` FOREIGN KEY (`compaction_id`) REFERENCES `compaction` (`id`)
 );



 CREATE TABLE `compaction_manual` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `bench_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `min_rpm` double DEFAULT NULL,
    `max_rpm` double DEFAULT NULL,
    `source` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `compaction_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_compaction_manual` (`compaction_id`),
    CONSTRAINT `fk_compaction_manual` FOREIGN KEY (`compaction_id`) REFERENCES `compaction` (`id`)
  );


CREATE TABLE `steam_curing` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `chamber` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `grade` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `entry_date` date DEFAULT NULL,
   `created_by` int DEFAULT NULL,
   `updated_by` int DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   PRIMARY KEY (`id`)
 );


CREATE TABLE `steam_curing_scada` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `date` date DEFAULT NULL,
   `time` time DEFAULT NULL,
   `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `ca1_set` double DEFAULT NULL,
   `ca1_actual` double DEFAULT NULL,
   `ca2_set` double DEFAULT NULL,
   `ca2_actual` double DEFAULT NULL,
   `fa_set` double DEFAULT NULL,
   `fa_actual` double DEFAULT NULL,
   `cement_set` double DEFAULT NULL,
   `cement_actual` double DEFAULT NULL,
   `water_set` double DEFAULT NULL,
   `water_actual` double DEFAULT NULL,
   `admixture_set` double DEFAULT NULL,
   `admixture_actual` double DEFAULT NULL,
   `total_set` double DEFAULT NULL,
   `total_actual` double DEFAULT NULL,
   `source` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `steam_curing_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_steam_scada` (`steam_curing_id`),
   CONSTRAINT `fk_steam_scada` FOREIGN KEY (`steam_curing_id`) REFERENCES `steam_curing` (`id`)
 );


 CREATE TABLE `steam_curing_manual` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `batch_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `chamber` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `min_temp` double DEFAULT NULL,
    `max_temp` double DEFAULT NULL,
    `source` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `steam_curing_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_steam_manual` (`steam_curing_id`),
    CONSTRAINT `fk_steam_manual` FOREIGN KEY (`steam_curing_id`) REFERENCES `steam_curing` (`id`)
  );

CREATE TABLE `bench_mould_inspection` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `line_shed_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `checking_date` date DEFAULT NULL,
   `bench_gang_no` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `sleeper_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `latest_casting_date` date DEFAULT NULL,
   `bench_visual_result` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `bench_dimensional_result` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `mould_visual_result` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `mould_dimensional_result` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `combined_remarks` text COLLATE utf8mb4_unicode_ci,
   `created_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `updated_by` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `created_date` datetime DEFAULT NULL,
   `updated_date` datetime DEFAULT NULL,
   `status` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   PRIMARY KEY (`id`)
 );


CREATE TABLE `steam_cube_sample_declaration` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `shed_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `line_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `casting_date` date DEFAULT NULL,
   `lbc_time` time DEFAULT NULL,
   `batch_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `concrete_grade` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `chamber_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `created_at` datetime DEFAULT NULL,
   PRIMARY KEY (`id`)
 );


CREATE TABLE `sample_cube` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `bench_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
   `sample_id` bigint DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `fk_sample_cube_sample` (`sample_id`),
   CONSTRAINT `fk_sample_cube_sample` FOREIGN KEY (`sample_id`) REFERENCES `steam_cube_sample_declaration` (`id`) ON DELETE CASCADE
 );


 CREATE TABLE `sample_other_bench` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `bench_no` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `sleeper_sequence` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `cube_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `sample_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `fk_other_bench_sample` (`sample_id`),
    CONSTRAINT `fk_other_bench_sample` FOREIGN KEY (`sample_id`) REFERENCES `steam_cube_sample_declaration` (`id`) ON DELETE CASCADE
  )


/////////////


ALTER TABLE compaction
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN time TIME;


   // plant id in the vendor plant
/////////////


SELECT * FROM steam_cube_sample_declaration

select *from steam_cube_testing

update steam_cube_sample_declaration set status = "Deleted" where id = 44



ALTER TABLE steam_cube_testing
ADD COLUMN sample_id BIGINT;


ALTER TABLE mf_test_details
ADD COLUMN shift VARCHAR(20),
ADD COLUMN vendor_code VARCHAR(50),
ADD COLUMN plant_id VARCHAR(50);



ALTER TABLE modulus_of_failure
ADD COLUMN shift VARCHAR(20),
ADD COLUMN vendor_code VARCHAR(50),
ADD COLUMN plant_id VARCHAR(50);



ALTER TABLE mor_sample_declaration
ADD COLUMN vendor_code VARCHAR(50),
ADD COLUMN plant_id VARCHAR(50);

select *from mor_sample_declaration
ALTER TABLE mor_test_result
ADD COLUMN shift VARCHAR(20),
ADD COLUMN vendor_code VARCHAR(50),
ADD COLUMN plant_id VARCHAR(50);

ALTER TABLE water_cube_sample_declaration
ADD COLUMN vendor_code VARCHAR(50),
ADD COLUMN plant_id VARCHAR(50);

ALTER TABLE water_cube_strength_test
ADD COLUMN vendor_code VARCHAR(50),
ADD COLUMN plant_id VARCHAR(50);







ALTER TABLE mf_test_details
ADD COLUMN batch_no VARCHAR(50),
ADD COLUMN casting_date DATE,
ADD COLUMN sample_identification VARCHAR(100),
ADD COLUMN concrete_grade VARCHAR(50);


ALTER TABLE mor_test_result
ADD COLUMN sampling_date DATE,
ADD COLUMN concrete_grade VARCHAR(50),
ADD COLUMN sample_identification_number VARCHAR(100);

done
///

ALTER TABLE steam_cube_testing
ADD COLUMN chamber_no varchar(50);



//
show create table po_ma_header

  ALTER TABLE po_ma_header
ADD COLUMN ref_no VARCHAR(100),
ADD COLUMN ref_date DATE,
ADD COLUMN request_id VARCHAR(50),
ADD COLUMN auth_seq VARCHAR(50),
ADD COLUMN auth_seq_fin VARCHAR(50),
ADD COLUMN curuser VARCHAR(50),
ADD COLUMN curuser_ind VARCHAR(50),
ADD COLUMN sign_id VARCHAR(50),
ADD COLUMN req_id VARCHAR(50),
ADD COLUMN rec_ind VARCHAR(10),
ADD COLUMN flag VARCHAR(10),
ADD COLUMN req_flag VARCHAR(10);


ALTER TABLE po_ma_detail
DROP FOREIGN KEY fk_ma_dtl_hdr;

ALTER TABLE po_ma_detail
ADD COLUMN ma_header_id BIGINT;

ALTER TABLE po_ma_detail
ADD CONSTRAINT fk_ma_dtl_hdr
FOREIGN KEY (ma_header_id) REFERENCES po_ma_header(id);

ALTER TABLE po_ma_detail
ADD COLUMN exp_sr VARCHAR(20),
ADD COLUMN exp_code VARCHAR(20),
ADD COLUMN cond_no VARCHAR(20),
ADD COLUMN orig_dp DATE,
ADD COLUMN payment_year VARCHAR(10),
ADD COLUMN new_posr_data VARCHAR(50),
ADD COLUMN ref_pono VARCHAR(50),
ADD COLUMN consignee_rly VARCHAR(20);


ALTER TABLE po_ma_header
ADD COLUMN auth_seq VARCHAR(50),
ADD COLUMN auth_seq_fin VARCHAR(50),
ADD COLUMN cur_user VARCHAR(50),
ADD COLUMN cur_user_ind VARCHAR(50),
ADD COLUMN sign_id VARCHAR(50),
ADD COLUMN req_id VARCHAR(50),
ADD COLUMN rec_ind VARCHAR(10),
ADD COLUMN flag VARCHAR(10),
ADD COLUMN req_flag VARCHAR(10),
ADD COLUMN request_id VARCHAR(50),
ADD COLUMN ref_no VARCHAR(100),
ADD COLUMN ref_date DATE;

ALTER TABLE po_ma_detail
DROP INDEX fk_ma_dtl_hdr;

ALTER TABLE po_ma_detail
ADD CONSTRAINT fk_ma_dtl_hdr
FOREIGN KEY (ma_header_id) REFERENCES po_ma_header(id);


CREATE TABLE et_epoxy_treated_sleeper (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    location VARCHAR(100),
    date_of_casting DATE,
    batch_number VARCHAR(100),
    sleeper_type VARCHAR(100),

    remark TEXT,
    is_confirmed BOOLEAN,

    shift VARCHAR(10),

    vendor_code VARCHAR(50),
    plant_id VARCHAR(50),

    created_by BIGINT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    updated_by BIGINT,
    updated_date TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP

);


CREATE TABLE et_sleeper_details (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    sleeper_id BIGINT,
    sleeper_no VARCHAR(50),

    et_id BIGINT,

    CONSTRAINT fk_et
    FOREIGN KEY (et_id)
    REFERENCES et_epoxy_treated_sleeper(id)
    ON DELETE CASCADE

);


CREATE TABLE final_call_inspection_section_a (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    call_no VARCHAR(100) UNIQUE,

    rly_po_no VARCHAR(200),
    po_date DATETIME,

    po_qty INT,
    vendor_name VARCHAR(255),

    ma_no VARCHAR(100),
    ma_date DATE,

    purchasing_authority VARCHAR(255),
    bill_paying_officer VARCHAR(255),

    plant_id VARCHAR(100),
    vendor_code VARCHAR(100),
    shift VARCHAR(50),

    created_by BIGINT,
    updated_by BIGINT,

    created_date DATETIME,
    updated_date DATETIME
);

CREATE TABLE final_call_inspection_section_b (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    call_no VARCHAR(100),

    inspection_call_date DATETIME,
    inspection_desired_date DATE,

    rly_po_sr VARCHAR(200),
    item_desc TEXT,

    product_type VARCHAR(100),
    type_of_erc VARCHAR(100),

    po_sr_qty_unit VARCHAR(100),
    consignee VARCHAR(255),

    orig_dp DATETIME,
    ext_dp DATETIME,
    orig_dp_start DATE,

    stage_of_inspection VARCHAR(100),
    call_qty_mt INT,

    place_of_inspection VARCHAR(255),

    process_ic_numbers VARCHAR(255),
    remarks TEXT,

    plant_id VARCHAR(100),
    vendor_code VARCHAR(100),
    shift VARCHAR(50),

    created_by BIGINT,
    updated_by BIGINT,

    created_date DATETIME,
    updated_date DATETIME
);

CREATE TABLE sleeper_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    call_no VARCHAR(100) NOT NULL,
    schedule_date DATE NOT NULL,
    reason VARCHAR(500),

    plant_id VARCHAR(100),
    vendor_code VARCHAR(100),
    shift VARCHAR(50),

    created_by BIGINT,
    updated_by BIGINT,

    created_date DATETIME,
    updated_date DATETIME
);
ALTER TABLE sleeper_workflow_transaction
ADD COLUMN job_status VARCHAR(50);


CREATE TABLE final_call_inspection_header (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    call_no VARCHAR(100) ,

    rly_po_no VARCHAR(100),
    po_date DATE,
    vendor_name VARCHAR(200),

    po_qty INT,
    ma_no VARCHAR(100),
    ma_date DATE,

    qty_offered_now INT,
    accepted_qty INT,
    rejected_qty INT,

    et_sleepers INT,
    call_date DATE,
    no_of_batches INT,

    shift VARCHAR(50),
    plant_id VARCHAR(100),
    vendor_code VARCHAR(100),

    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    created_date TIMESTAMP,
    updated_date TIMESTAMP
);

CREATE TABLE ie_batch_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    batch_no VARCHAR(100),
    call_no VARCHAR(100),

    date_casted DATE,

    casted DECIMAL(18,2),
    offered_prev DECIMAL(18,2),
    offered_now DECIMAL(18,2),

    passed DECIMAL(18,2),
    rejected DECIMAL(18,2),

    total_offered DECIMAL(18,2),
    total_accepted DECIMAL(18,2),
    total_rejected DECIMAL(18,2),

    shift VARCHAR(50),
    plant_id VARCHAR(100),
    vendor_code VARCHAR(100),

    created_by VARCHAR(100),
    updated_by VARCHAR(100),

    created_date TIMESTAMP,
    updated_date TIMESTAMP
);

CREATE TABLE final_good_sleepers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    sleeper_id BIGINT,
    sleeper_code VARCHAR(100),

    batch_id BIGINT,

    CONSTRAINT fk_good_batch
        FOREIGN KEY (batch_id)
        REFERENCES ie_batch_summary(id)
        ON DELETE CASCADE
);

CREATE TABLE final_call_rejected_sleepers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    sleeper_id BIGINT,
    sleeper_code VARCHAR(100),

    reason VARCHAR(255),
    type VARCHAR(100),

    batch_id BIGINT,

    CONSTRAINT fk_rejected_batch
        FOREIGN KEY (batch_id)
        REFERENCES ie_batch_summary(id)
        ON DELETE CASCADE
);


CREATE TABLE final_call_et_sleeper (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    sleeper_id BIGINT,
    sleeper_code VARCHAR(100),

    batch_id BIGINT,

    CONSTRAINT fk_et_batch
        FOREIGN KEY (batch_id)
        REFERENCES ie_batch_summary(id)
        ON DELETE CASCADE
);

CREATE TABLE ie_mf_sleepers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    sleeper_id BIGINT,
    sleeper_code VARCHAR(100),

    batch_id BIGINT,

    CONSTRAINT fk_mf_batch
        FOREIGN KEY (batch_id)
        REFERENCES ie_batch_summary(id)
        ON DELETE CASCADE
);

CREATE TABLE final_inspection_rejections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    sleeper_id BIGINT,
    sleeper_code VARCHAR(100),

    reason VARCHAR(255),

    batch_id BIGINT,

    CONSTRAINT fk_final_rejection_batch
        FOREIGN KEY (batch_id)
        REFERENCES ie_batch_summary(id)
        ON DELETE CASCADE
);
/////


ALTER TABLE production_sleeper
ADD COLUMN sleeper_type VARCHAR(50);


ALTER TABLE wire_tensioning
ADD COLUMN location VARCHAR(50);

