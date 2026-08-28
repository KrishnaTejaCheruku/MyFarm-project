CREATE TABLE catalog_category (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(64) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    name_en VARCHAR(160) NOT NULL,
    name_te VARCHAR(160) NULL,
    sort_order INT UNSIGNED NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_catalog_category PRIMARY KEY (id),
    CONSTRAINT uk_catalog_category_code UNIQUE (code),
    CONSTRAINT uk_catalog_category_slug UNIQUE (slug),
    INDEX idx_catalog_category_browse (active, sort_order, id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE catalog_product (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    category_id BIGINT UNSIGNED NOT NULL,
    code VARCHAR(64) NOT NULL,
    slug VARCHAR(160) NOT NULL,
    name_en VARCHAR(200) NOT NULL,
    name_te VARCHAR(200) NULL,
    description_en VARCHAR(2000) NULL,
    description_te VARCHAR(2000) NULL,
    sort_order INT UNSIGNED NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_catalog_product PRIMARY KEY (id),
    CONSTRAINT uk_catalog_product_code UNIQUE (code),
    CONSTRAINT uk_catalog_product_slug UNIQUE (slug),
    CONSTRAINT fk_catalog_product_category
        FOREIGN KEY (category_id) REFERENCES catalog_category (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    INDEX idx_catalog_product_browse (active, sort_order, id),
    INDEX idx_catalog_product_category
        (category_id, active, sort_order, id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE catalog_variant (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    product_id BIGINT UNSIGNED NOT NULL,
    sku VARCHAR(80) NOT NULL,
    quantity DECIMAL(10, 3) NOT NULL,
    unit VARCHAR(24) NOT NULL,
    price_minor BIGINT UNSIGNED NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    price_tax_inclusive BOOLEAN NOT NULL DEFAULT TRUE,
    gst_basis_points SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    subscription_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    image_key VARCHAR(255) NULL,
    sort_order INT UNSIGNED NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_catalog_variant PRIMARY KEY (id),
    CONSTRAINT uk_catalog_variant_sku UNIQUE (sku),
    CONSTRAINT fk_catalog_variant_product
        FOREIGN KEY (product_id) REFERENCES catalog_product (id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,
    CONSTRAINT chk_catalog_variant_quantity CHECK (quantity > 0),
    CONSTRAINT chk_catalog_variant_gst
        CHECK (gst_basis_points <= 10000),
    INDEX idx_catalog_variant_product
        (product_id, active, sort_order, id)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
