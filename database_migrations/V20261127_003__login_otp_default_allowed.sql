-- Login OTP fallback when SMS does not reach users.
-- default_allowed: SMS OTP was sent but the default OTP 123456 is also accepted for this login
--                  (SMS outage fallback, or the user's previous SMS OTP was never used).
-- used_default:    the login used 123456 although a real SMS OTP was sent.
-- Run BEFORE deploying the backend (the LoginOtp entity reads these columns).

ALTER TABLE login_otp
    ADD COLUMN default_allowed TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN used_default    TINYINT(1) NOT NULL DEFAULT 0;

-- check
SELECT column_name, column_type, column_default FROM information_schema.columns
WHERE table_schema = DATABASE() AND table_name = 'login_otp'
  AND column_name IN ('default_allowed', 'used_default');
