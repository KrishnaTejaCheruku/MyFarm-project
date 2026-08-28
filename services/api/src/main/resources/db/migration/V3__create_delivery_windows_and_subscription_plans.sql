CREATE TABLE delivery_window (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    service_area_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(64) NOT NULL,
    name_en VARCHAR(160) NOT NULL,
    name_te VARCHAR(160) NULL,
    starts_at TIME NOT NULL,
    ends_at TIME NOT NULL,
    cutoff_minutes_before INT UNSIGNED NOT NULL,
    sort_order INT UNSIGNED NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_delivery_window PRIMARY KEY (id),
    CONSTRAINT uk_delivery_window_area_code UNIQUE (service_area_id, code),
    CONSTRAINT fk_delivery_window_area
        FOREIGN KEY (service_area_id) REFERENCES delivery_service_area (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_delivery_window_times CHECK (starts_at < ends_at),
    INDEX idx_delivery_window_browse
        (service_area_id, active, sort_order, id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE delivery_subscription_plan (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    service_area_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(64) NOT NULL,
    name_en VARCHAR(160) NOT NULL,
    name_te VARCHAR(160) NULL,
    billing_period VARCHAR(16) NOT NULL,
    duration_months SMALLINT UNSIGNED NOT NULL,
    sort_order INT UNSIGNED NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_delivery_subscription_plan PRIMARY KEY (id),
    CONSTRAINT uk_delivery_subscription_plan_area_code
        UNIQUE (service_area_id, code),
    CONSTRAINT fk_delivery_subscription_plan_area
        FOREIGN KEY (service_area_id) REFERENCES delivery_service_area (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_delivery_subscription_plan_period
        CHECK (billing_period IN ('MONTHLY', 'YEARLY')),
    CONSTRAINT chk_delivery_subscription_plan_duration CHECK (
        (billing_period = 'MONTHLY' AND duration_months = 1)
        OR (billing_period = 'YEARLY' AND duration_months = 12)
    ),
    INDEX idx_delivery_subscription_plan_browse
        (service_area_id, active, sort_order, id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
