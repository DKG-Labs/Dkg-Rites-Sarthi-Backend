
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