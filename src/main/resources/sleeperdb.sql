
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
