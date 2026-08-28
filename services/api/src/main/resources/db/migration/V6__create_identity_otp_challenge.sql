CREATE TABLE identity_otp_challenge (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    phone VARCHAR(10) NOT NULL,
    -- SHA-256 hex digest of the 6-digit code -- the code itself is
    -- never persisted, same reasoning as a password hash even though
    -- this is short-lived (5 minutes, see OtpService).
    code_hash VARCHAR(64) NOT NULL,
    attempts INT UNSIGNED NOT NULL DEFAULT 0,
    expires_at DATETIME(6) NOT NULL,
    consumed_at DATETIME(6) NULL,
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_identity_otp_challenge PRIMARY KEY (id),
    CONSTRAINT chk_identity_otp_challenge_phone
        CHECK (phone REGEXP '^[6-9][0-9]{9}$'),
    INDEX idx_identity_otp_challenge_phone_created (phone, created_at)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
