DELETE FROM commerce_order_item;
DELETE FROM commerce_order;
DELETE FROM catalog_variant;
DELETE FROM catalog_product;
DELETE FROM catalog_category;
DELETE FROM delivery_window;
DELETE FROM delivery_subscription_plan;
DELETE FROM delivery_service_area_pincode;
DELETE FROM delivery_service_area;

INSERT INTO delivery_service_area (
    id, code, name_en, name_te, city, state, timezone,
    subscription_required, active, version
) VALUES
    (
        6101, 'orders-test-area', 'Orders Test Area', NULL,
        'Visakhapatnam', 'Andhra Pradesh', 'Asia/Kolkata',
        FALSE, TRUE, 0
    ),
    (
        6102, 'orders-test-inactive-area', 'Inactive Area', NULL,
        'Visakhapatnam', 'Andhra Pradesh', 'Asia/Kolkata',
        FALSE, FALSE, 0
    );

INSERT INTO delivery_window (
    id, service_area_id, code, name_en, name_te, starts_at, ends_at,
    cutoff_minutes_before, sort_order, active, version
) VALUES
    (
        7101, 6101, 'morning', 'Morning', NULL, '05:30:00', '08:30:00',
        480, 10, TRUE, 0
    ),
    (
        7102, 6101, 'inactive-window', 'Inactive', NULL,
        '09:00:00', '10:00:00', 60, 20, FALSE, 0
    );

INSERT INTO catalog_category (
    id, code, slug, name_en, name_te, sort_order, active, version
) VALUES
    (
        5101, 'orders-test-category', 'orders-test-category',
        'Test Category', NULL, 10, TRUE, 0
    );

INSERT INTO catalog_product (
    id, category_id, code, slug, name_en, name_te,
    description_en, description_te, sort_order, active, version
) VALUES
    (
        5201, 5101, 'orders-test-product', 'orders-test-product',
        'Test Tomato', 'టెస్ట్ టమాటో', NULL, NULL, 10, TRUE, 0
    );

INSERT INTO catalog_variant (
    id, product_id, sku, quantity, unit, price_inr, currency,
    price_tax_inclusive, gst_basis_points, subscription_allowed,
    image_key, sort_order, active, version
) VALUES
    (
        5301, 5201, 'ORD-TEST-1KG', 1.000, 'KILOGRAM', 40, 'INR',
        TRUE, 0, FALSE, NULL, 10, TRUE, 0
    ),
    (
        5302, 5201, 'ORD-TEST-INACTIVE', 1.000, 'KILOGRAM', 40, 'INR',
        TRUE, 0, FALSE, NULL, 20, FALSE, 0
    );
