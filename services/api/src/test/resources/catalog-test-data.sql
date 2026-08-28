DELETE FROM catalog_variant;
DELETE FROM catalog_product;
DELETE FROM catalog_category;

INSERT INTO catalog_category (
    id, code, slug, name_en, name_te, sort_order, active, version
) VALUES
    (1001, 'milk', 'milk', 'Milk', 'పాలు', 10, TRUE, 0),
    (1002, 'eggs', 'eggs', 'Eggs', 'గుడ్లు', 20, TRUE, 0),
    (1003, 'hidden', 'hidden', 'Hidden', NULL, 30, FALSE, 0);

INSERT INTO catalog_product (
    id, category_id, code, slug, name_en, name_te,
    description_en, description_te, sort_order, active, version
) VALUES
    (
        2001, 1001, 'cow-milk', 'cow-milk', 'Cow Milk', 'ఆవు పాలు',
        'Fresh cow milk.', 'తాజా ఆవు పాలు.', 10, TRUE, 0
    ),
    (
        2002, 1002, 'farm-eggs', 'farm-eggs', 'Farm Eggs', 'నాటు గుడ్లు',
        'Free-range farm eggs.', NULL, 20, TRUE, 0
    ),
    (
        2003, 1001, 'inactive-milk', 'inactive-milk',
        'Inactive Milk', NULL, NULL, NULL, 30, FALSE, 0
    );

INSERT INTO catalog_variant (
    id, product_id, sku, quantity, unit, price_inr, currency,
    price_tax_inclusive, gst_basis_points, subscription_allowed,
    image_key, sort_order, active, version
) VALUES
    (
        3001, 2001, 'MILK-COW-500ML', 500.000, 'MILLILITRE', 38, 'INR',
        TRUE, 0, TRUE, 'catalog/milk/cow-500ml.jpg', 10, TRUE, 0
    ),
    (
        3002, 2001, 'MILK-COW-1L', 1.000, 'LITRE', 72, 'INR',
        TRUE, 0, TRUE, 'catalog/milk/cow-1l.jpg', 20, TRUE, 0
    ),
    (
        3003, 2001, 'MILK-COW-INACTIVE', 2.000, 'LITRE', 140, 'INR',
        TRUE, 0, FALSE, NULL, 30, FALSE, 0
    ),
    (
        3004, 2002, 'EGG-FARM-6', 6.000, 'PIECE', 110, 'INR',
        TRUE, 0, FALSE, 'catalog/eggs/farm-6.jpg', 10, TRUE, 0
    );
