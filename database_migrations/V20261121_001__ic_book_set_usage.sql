-- Book No + Set No can be used on only one IC (ERC RM/Process/Final, Sleeper Final, Rail Pad Process/Final).
-- Applies from now on: the table starts empty, old ICs are not back-filled.
-- The backend writes a row on every Save Changes / Edit save and reads it on "Verify Book & Set".

CREATE TABLE IF NOT EXISTS ic_book_set_usage (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  book_no       VARCHAR(50)  NOT NULL,          -- trimmed, upper case
  set_no        VARCHAR(20)  NOT NULL,          -- trimmed, upper case
  ic_key        VARCHAR(100) NOT NULL,          -- call no of the IC (certificate R/CALL/USER reduced to CALL)
  ic_number     VARCHAR(150) NULL,              -- IC number as saved
  product_stage VARCHAR(30)  NULL,              -- ERC_RM, ERC_PROCESS, ERC_FINAL, SLEEPER_FINAL, RAILPAD_PROCESS, RAILPAD_FINAL
  user_id       VARCHAR(50)  NULL,              -- user ID from the login token
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NULL,
  UNIQUE KEY uk_ic_book_set (book_no, set_no),
  KEY idx_ic_book_set_ic_key (ic_key)
);

-- Check
SELECT COUNT(*) AS rows_now FROM ic_book_set_usage;
