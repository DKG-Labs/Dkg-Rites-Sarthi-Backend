
CREATE TABLE rail_workflow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    workflow_name VARCHAR(255),
    created_by BIGINT,
    created_date DATETIME
);

CREATE TABLE rail_module (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module_name VARCHAR(255),
    created_by BIGINT,
    created_date DATETIME,
    workflow_id BIGINT,

    CONSTRAINT fk_rail_module_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES rail_workflow(id)
);

CREATE TABLE rail_workflow_transaction (
    workflow_transition_id INT PRIMARY KEY AUTO_INCREMENT,

    workflow_id BIGINT,
    module_id BIGINT,

    request_id VARCHAR(255),
    action VARCHAR(255),
    status VARCHAR(255),

    remarks TEXT,

    current_role VARCHAR(255),
    next_role VARCHAR(255),

    shift VARCHAR(100),

    poi_code VARCHAR(255),
    rio VARCHAR(255),

    assigned_to_user BIGINT,

    vendor_code VARCHAR(255),
    plant_id VARCHAR(255),

    created_by BIGINT,
    modified_by BIGINT,

    created_date DATETIME,
    updated_date DATETIME,

    job_status VARCHAR(255),

    CONSTRAINT fk_workflow_transaction_workflow
        FOREIGN KEY (workflow_id)
        REFERENCES rail_workflow(id),

    CONSTRAINT fk_workflow_transaction_module
        FOREIGN KEY (module_id)
        REFERENCES rail_module(id)
);

CREATE TABLE RAIL_TRANSITION_MASTER (

    TRANSITIONID INT PRIMARY KEY AUTO_INCREMENT,

    TRANSITIONNAME VARCHAR(255) NOT NULL,

    WORKFLOWID INT NOT NULL,

    CURRENTROLEID INT NOT NULL,

    NEXTROLEID INT NOT NULL,

    TRANSITIONORDER INT,

    CREATEDBY VARCHAR(255),

    CURRENT_ACTION VARCHAR(255),

    NEXT_ACTION VARCHAR(255),

    CREATEDDATE DATETIME DEFAULT CURRENT_TIMESTAMP
);


