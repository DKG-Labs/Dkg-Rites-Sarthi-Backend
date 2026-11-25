-- Super Admin: temporary change of a PO serial's DP (po_item.delivery_date), auto-reverted after 2 hours.
-- One row per change. affected_rows (JSON) keeps every row touched (po_item + amendment_po_item copies)
-- with its previous value, so the revert restores exactly what was there.
-- Safe to re-run.

CREATE TABLE IF NOT EXISTS super_admin_dp_change (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_no                VARCHAR(50)  NOT NULL,
    item_sr_no           VARCHAR(20)  NOT NULL,
    old_delivery_date    DATETIME     NULL,
    new_delivery_date    DATETIME     NOT NULL,
    affected_rows        TEXT         NOT NULL,
    remarks              VARCHAR(500) NOT NULL,
    changed_by           INT          NULL,
    changed_by_emp_code  VARCHAR(50)  NULL,
    changed_by_name      VARCHAR(150) NULL,
    changed_at           DATETIME     NOT NULL,
    revert_at            DATETIME     NOT NULL,
    status               VARCHAR(20)  NOT NULL,   -- ACTIVE / REVERTED / REVERTED_MANUAL / SKIPPED
    reverted_at          DATETIME     NULL,
    reverted_by          INT          NULL,
    reverted_by_name     VARCHAR(150) NULL,
    revert_note          VARCHAR(500) NULL,
    KEY idx_dp_change_due (status, revert_at),
    KEY idx_dp_change_po (po_no, item_sr_no)
);
