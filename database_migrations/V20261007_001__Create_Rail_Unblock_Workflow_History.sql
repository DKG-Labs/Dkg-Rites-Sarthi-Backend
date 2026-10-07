-- Create table for tracking unblock actions on Rail Pad workflow modules
CREATE TABLE IF NOT EXISTS Rail_unblock_workflow_hitory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(100) NOT NULL,
    module_id BIGINT NOT NULL,
    module_name VARCHAR(100),
    workflow_id BIGINT,
    plant_id VARCHAR(50),
    vendor_code VARCHAR(50),
    shift VARCHAR(50),
    previous_status VARCHAR(50),
    previous_action VARCHAR(50),
    previous_remarks TEXT,
    unblocked_by BIGINT,
    unblocked_by_name VARCHAR(150),
    unblocked_by_role VARCHAR(100),
    unblock_remarks TEXT,
    unblocked_on DATETIME
);
