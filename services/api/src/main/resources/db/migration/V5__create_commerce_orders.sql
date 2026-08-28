CREATE TABLE commerce_order (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(32) NOT NULL,
    -- snapshots of the delivery module's codes, not FKs: an order
    -- shouldn't change meaning if the area/window it named is later
    -- edited or deactivated.
    service_area_code VARCHAR(64) NOT NULL,
    delivery_window_code VARCHAR(64) NOT NULL,
    customer_name VARCHAR(120) NOT NULL,
    customer_phone VARCHAR(10) NOT NULL,
    delivery_address_line1 VARCHAR(200) NOT NULL,
    delivery_address_line2 VARCHAR(200) NULL,
    delivery_pincode VARCHAR(6) NOT NULL,
    payment_method VARCHAR(16) NOT NULL,
    status VARCHAR(20) NOT NULL,
    subtotal_inr BIGINT UNSIGNED NOT NULL DEFAULT 0,
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_commerce_order PRIMARY KEY (id),
    CONSTRAINT uk_commerce_order_number UNIQUE (order_number),
    CONSTRAINT chk_commerce_order_pincode
        CHECK (delivery_pincode REGEXP '^[0-9]{6}$'),
    CONSTRAINT chk_commerce_order_phone
        CHECK (customer_phone REGEXP '^[6-9][0-9]{9}$'),
    INDEX idx_commerce_order_phone_created (customer_phone, created_at)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Write-once line items -- a snapshot of what a variant was called and
-- cost at order time, never updated afterward, so no version column.
CREATE TABLE commerce_order_item (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    order_id BIGINT UNSIGNED NOT NULL,
    variant_id BIGINT UNSIGNED NOT NULL,
    sku VARCHAR(80) NOT NULL,
    name_en VARCHAR(200) NOT NULL,
    name_te VARCHAR(200) NULL,
    quantity INT UNSIGNED NOT NULL,
    unit_price_inr BIGINT UNSIGNED NOT NULL,
    line_total_inr BIGINT UNSIGNED NOT NULL,
    CONSTRAINT pk_commerce_order_item PRIMARY KEY (id),
    CONSTRAINT fk_commerce_order_item_order
        FOREIGN KEY (order_id) REFERENCES commerce_order (id)
        ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT chk_commerce_order_item_quantity CHECK (quantity > 0),
    INDEX idx_commerce_order_item_order (order_id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
