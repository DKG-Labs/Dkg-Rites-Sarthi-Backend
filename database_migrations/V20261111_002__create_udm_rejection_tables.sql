-- =====================================================================
-- V20261111_002 : Create UDM rejection tables (missing in this sarthidb)
-- Matches entities com.sarthi.entity.UdmRejection and UdmSyncLog.
-- Safe to re-run (CREATE TABLE IF NOT EXISTS). Run BEFORE V20261111_001 is used.
-- =====================================================================

-- Pre-check (read only): which DB am I on, and do the tables exist?
-- SELECT DATABASE();
-- SHOW TABLES LIKE 'udm_%';

CREATE TABLE IF NOT EXISTS udm_rejection (
    id                      BIGINT          NOT NULL AUTO_INCREMENT,
    po_number               VARCHAR(50)     NULL,
    po_date                 DATETIME        NULL,
    pl_number               VARCHAR(50)     NULL,
    po_serial_number        VARCHAR(20)     NULL,
    rejection_number        VARCHAR(100)    NOT NULL,
    rejection_date          DATETIME        NULL,
    type_of_rejection       VARCHAR(100)    NULL,
    rejected_qty            DECIMAL(15,3)   NULL,
    uom                     VARCHAR(30)     NULL,
    reason_for_rejection    TEXT            NULL,
    rejection_value         DECIMAL(18,2)   NULL,
    status                  VARCHAR(20)     NULL,
    item_description        TEXT            NULL,
    ic_number               VARCHAR(100)    NULL,
    ic_date                 DATETIME        NULL,
    inspection_agency       VARCHAR(100)    NULL,
    vendor_code             VARCHAR(50)     NULL,
    vendor_name             VARCHAR(255)    NULL,
    consignee               VARCHAR(500)    NULL,
    material_received_date  DATETIME        NULL,
    source                  VARCHAR(50)     NULL,
    pdf_path                VARCHAR(500)    NULL,
    fetched_date            DATE            NOT NULL,
    created_at              DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_rejection_number (rejection_number),
    KEY idx_udm_rej_fetched (fetched_date, rejection_date),
    KEY idx_udm_rej_po (po_number),
    KEY idx_udm_rej_ic (ic_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS udm_sync_log (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    sync_date         DATE          NOT NULL,
    trigger_type      VARCHAR(20)   NOT NULL,
    triggered_by      VARCHAR(150)  NULL,
    started_at        DATETIME      NOT NULL,
    finished_at       DATETIME      NULL,
    duration_ms       BIGINT        NULL,
    status            VARCHAR(20)   NOT NULL,
    records_received  INT           NULL,
    records_new       INT           NULL,
    records_updated   INT           NULL,
    records_failed    INT           NULL,
    error_message     TEXT          NULL,
    PRIMARY KEY (id),
    KEY idx_udm_sync_started (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Verify (read only)
-- SHOW CREATE TABLE udm_rejection;
-- SHOW CREATE TABLE udm_sync_log;
