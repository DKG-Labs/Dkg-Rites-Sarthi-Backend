-- ============================================================
-- SARTHI Backend - Database Schema Reference
-- Database: sarthiworkflow
-- User Authentication & Role Management Tables
-- ============================================================
-- NOTE: These tables already exist in sarthiworkflow database
-- This file is for reference only - DO NOT RUN if tables exist
-- ============================================================

-- ============================================================
-- TABLE: user_master
-- Stores user credentials and basic information for login
-- ============================================================
/*
CREATE TABLE IF NOT EXISTS user_master (
    userId              INT             AUTO_INCREMENT PRIMARY KEY,
    password            VARCHAR(100)    NOT NULL,
    userName            VARCHAR(100)    NULL,
    email               VARCHAR(255)    NULL,
    mobileNumber        VARCHAR(10)     NULL,
    createdDate         DATETIME        NULL,
    createdBy           VARCHAR(45)     NULL,
    role_name           VARCHAR(255)    NULL,
    employee_id         VARCHAR(50)     NULL
);
*/

-- ============================================================
-- TABLE: role_master
-- Stores available roles in the system
-- ============================================================
/*
CREATE TABLE IF NOT EXISTS role_master (
    ROLEID              INT             AUTO_INCREMENT PRIMARY KEY,
    ROLENAME            VARCHAR(100)    NOT NULL,
    CREATEDBY           VARCHAR(50)     NULL,
    CREATEDDATE         DATETIME        NULL
);
*/

-- ============================================================
-- TABLE: user_role_master
-- Maps users to roles with permissions
-- ============================================================
/*
CREATE TABLE IF NOT EXISTS user_role_master (
    userRoleId          INT             AUTO_INCREMENT PRIMARY KEY,
    userId              INT             NOT NULL,
    roleId              INT             NOT NULL,
    readPermission      TINYINT(1)      NOT NULL,
    writePermission     TINYINT(1)      NOT NULL,
    createdDate         DATETIME        NULL,
    createdBy           VARCHAR(45)     NULL
);
*/

-- ============================================================
-- EXISTING ROLES IN DATABASE:
-- ============================================================
-- 1: Vendor
-- 2: RIO Help Desk
-- 3: IE
-- 4: IE Secondary
-- 5: Control Manager
-- 6: Rio Finance
-- 7: Process IE
-- 8: SBU Head

-- ============================================================
-- LOGIN CREDENTIALS (Existing Users):
-- ============================================================
-- User ID: 1,  Password: password, Role: Vendor
-- User ID: 13, Password: password, Role: IE (Inspector Engineer)
-- User ID: 20, Password: password, Role: Control Manager
-- User ID: 21, Password: password, Role: Process IE




ALTER TABLE amendment_po_item
ADD COLUMN po_key VARCHAR(50);


ALTER TABLE po_header
ADD COLUMN is_amended TINYINT(1) DEFAULT 0,
ADD COLUMN amendment_count INT DEFAULT 0,
ADD COLUMN last_amendment_no VARCHAR(50),
ADD COLUMN last_amendment_date DATETIME;





ALTER TABLE ibs_call_registration
ADD COLUMN billing_status VARCHAR(30) NOT NULL DEFAULT 'PENDING';

CREATE TABLE ibs_bill_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    ibs_call_registration_id BIGINT NOT NULL,

    bill_no VARCHAR(50),
    invoice_no VARCHAR(100),
    invoice_date DATETIME,

    case_no VARCHAR(50),
    call_date DATETIME,
    call_sno INT,

    bk_no VARCHAR(50),
    set_no VARCHAR(50),

    invoice_pdf TEXT,
    invoice_supp_docs TEXT,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_bill_call
        FOREIGN KEY (ibs_call_registration_id)
        REFERENCES ibs_call_registration(id),

    UNIQUE KEY uk_bill_no_call_sno (bill_no, call_sno),

    INDEX idx_case_no (case_no),
    INDEX idx_call_sno (call_sno)
);
CREATE TABLE ibs_payment_details (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    ibs_call_registration_id BIGINT NOT NULL,

    case_no VARCHAR(50),

    call_recv_dt DATETIME,

    call_sno INT,

    description VARCHAR(500),

    mer_txn_id VARCHAR(200),

    amount DECIMAL(18,2),

    cust_email VARCHAR(200),

    cust_mobile VARCHAR(20),

    txn_complete_date DATETIME,

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_call
        FOREIGN KEY (ibs_call_registration_id)
        REFERENCES ibs_call_registration(id),

    UNIQUE KEY uk_mer_txn_id (mer_txn_id),

    INDEX idx_case_no (case_no),
    INDEX idx_call_sno (call_sno)
);

