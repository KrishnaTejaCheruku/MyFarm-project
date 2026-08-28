CREATE TABLE delivery_service_area (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    name_en VARCHAR(160) NOT NULL,
    name_te VARCHAR(160) NULL,
    city VARCHAR(120) NOT NULL,
    state VARCHAR(120) NOT NULL,
    timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Kolkata',
    subscription_required BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_delivery_service_area PRIMARY KEY (id),
    CONSTRAINT uk_delivery_service_area_code UNIQUE (code),
    INDEX idx_delivery_service_area_browse (active, name_en, id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE delivery_service_area_pincode (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    service_area_id BIGINT UNSIGNED NOT NULL,
    pincode VARCHAR(6) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_delivery_service_area_pincode PRIMARY KEY (id),
    CONSTRAINT uk_delivery_service_area_pincode
        UNIQUE (service_area_id, pincode),
    CONSTRAINT fk_delivery_service_area_pincode_area
        FOREIGN KEY (service_area_id) REFERENCES delivery_service_area (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_delivery_service_area_pincode
        CHECK (pincode REGEXP '^[0-9]{6}$'),
    INDEX idx_delivery_service_area_pincode_lookup
        (pincode, active, service_area_id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
