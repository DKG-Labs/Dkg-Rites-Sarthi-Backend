-- =====================================================================
-- V20261124_001 : Log of SMS sent on call assignment / reassignment (ERC, Sleeper, Rail Pad)
-- One row per recipient per event: SENT / FAILED / SKIPPED (with reason).
-- Mobile numbers are stored masked only.  Safe to re-run.
-- =====================================================================

CREATE TABLE IF NOT EXISTS call_sms_log (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  product           VARCHAR(10)  NOT NULL,   -- ERC / SLEEPER / RAILPAD
  call_no           VARCHAR(50)  NOT NULL,
  event_type        VARCHAR(20)  NOT NULL,   -- ASSIGNED / REASSIGNED
  recipient_type    VARCHAR(10)  NOT NULL,   -- IE / OLD_IE / VENDOR / CM
  recipient_user_id INT          NULL,
  ie_user_id        INT          NULL,       -- IE the call is (re)assigned to (dedupe key)
  mobile_masked     VARCHAR(20)  NULL,       -- e.g. 98XXXXXX10
  template_key      VARCHAR(40)  NOT NULL,
  status            VARCHAR(10)  NOT NULL,   -- SENT / FAILED / SKIPPED
  reason            VARCHAR(255) NULL,
  created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_csl_call (call_no),
  KEY idx_csl_dedupe (call_no, event_type, recipient_type, ie_user_id, status),
  KEY idx_csl_created (created_at)
);

-- VERIFY (read only)
SELECT COUNT(*) AS rows_in_call_sms_log FROM call_sms_log;

-- ---------------------------------------------------------------------
-- application.properties (add per environment, values from the DLT registration):
--   xpertsms.call-notifications.enabled=true
--   xpertsms.template.call.ie-assigned=<DLT template id>
--   xpertsms.template.call.vendor-assigned=<DLT template id>
--   xpertsms.template.call.cm-assigned=<DLT template id>
--   xpertsms.template.call.ie-reassigned-new=<DLT template id>
--   xpertsms.template.call.ie-reassigned-old=<DLT template id>
--   xpertsms.template.call.vendor-reassigned=<DLT template id>
--   xpertsms.template.call.cm-reassigned=<DLT template id>
-- Until a template id is set, that SMS is logged as SKIPPED (nothing is sent).
-- ---------------------------------------------------------------------
