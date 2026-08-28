INSERT INTO catalog_category (code, slug, name_en, name_te, sort_order, active) VALUES
    ('vegetables', 'vegetables', 'Vegetables', 'కూరగాయలు', 10, TRUE),
    ('leafy-greens', 'leafy-greens', 'Leafy Greens', 'ఆకు కూరలు', 20, TRUE),
    ('fruits', 'fruits', 'Fruits', 'పండ్లు', 30, TRUE),
    ('dairy', 'dairy', 'Dairy', 'పాల ఉత్పత్తులు', 40, TRUE),
    ('grains-pulses', 'grains-pulses', 'Grains & Pulses', 'ధాన్యాలు', 50, TRUE);

INSERT INTO catalog_product (
    category_id, code, slug, name_en, name_te,
    description_en, description_te, sort_order, active
) VALUES
    (
        (SELECT id FROM catalog_category WHERE code = 'vegetables'),
        'tomato', 'tomato', 'Tomato', 'టమాటా',
        'Farm-fresh, vine-ripened tomatoes.', 'తాజా టమాటాలు',
        10, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'vegetables'),
        'onion', 'onion', 'Onion', 'ఉల్లిపాయ',
        'Everyday red onions.', 'ఎర్ర ఉల్లిపాయలు',
        20, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'vegetables'),
        'potato', 'potato', 'Potato', 'బంగాళదుంప',
        'All-purpose potatoes.', 'బంగాళదుంపలు',
        30, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'vegetables'),
        'brinjal', 'brinjal', 'Brinjal', 'వంకాయ',
        'Purple brinjal, good for curries.', 'వంకాయలు',
        40, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'leafy-greens'),
        'spinach', 'spinach', 'Spinach', 'పాలకూర',
        'Freshly cut spinach bunches.', 'తాజా పాలకూర కట్టలు',
        10, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'leafy-greens'),
        'gongura', 'gongura', 'Gongura', 'గోంగూర',
        'Tangy sorrel leaves, an Andhra favorite.',
        'పులుపు ఆకు కూర',
        20, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'leafy-greens'),
        'coriander', 'coriander', 'Coriander', 'కొత్తిమీర',
        'Fresh coriander bunches.', 'తాజా కొత్తిమీర కట్టలు',
        30, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'fruits'),
        'banana', 'banana', 'Banana', 'అరటిపండు',
        'Sweet, ripe bananas.', 'తియ్యని అరటిపండ్లు',
        10, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'fruits'),
        'mango', 'mango', 'Mango (Banganapalli)', 'మామిడి పండు',
        'Banganapalli mangoes, in season.',
        'బంగినపల్లి మామిడిపండ్లు',
        20, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'fruits'),
        'papaya', 'papaya', 'Papaya', 'బొప్పాయి',
        'Ripe papaya, sold whole.', 'పండిన బొప్పాయి',
        30, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'dairy'),
        'milk', 'milk', 'Cow Milk', 'ఆవు పాలు',
        'Fresh cow milk, delivered daily.', 'తాజా ఆవు పాలు',
        10, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'dairy'),
        'curd', 'curd', 'Curd', 'పెరుగు',
        'Thick, set curd.', 'చిక్కటి పెరుగు',
        20, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'dairy'),
        'paneer', 'paneer', 'Paneer', 'పన్నీర్',
        'Fresh paneer block.', 'తాజా పన్నీర్',
        30, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'grains-pulses'),
        'rice-sona-masoori', 'rice-sona-masoori', 'Sona Masoori Rice',
        'సోనా మసూరి బియ్యం',
        'Everyday Sona Masoori rice.', 'సోనా మసూరి బియ్యం',
        10, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'grains-pulses'),
        'toor-dal', 'toor-dal', 'Toor Dal', 'కంది పప్పు',
        'Split pigeon peas.', 'కంది పప్పు',
        20, TRUE
    ),
    (
        (SELECT id FROM catalog_category WHERE code = 'grains-pulses'),
        'ragi', 'ragi', 'Ragi (Finger Millet)', 'రాగులు',
        'Whole ragi grain.', 'రాగి గింజలు',
        30, TRUE
    );

INSERT INTO catalog_variant (
    product_id, sku, quantity, unit, price_inr, currency,
    price_tax_inclusive, gst_basis_points, subscription_allowed,
    image_key, sort_order, active
) VALUES
    ((SELECT id FROM catalog_product WHERE code = 'tomato'), 'TOMATO-1KG', 1.000, 'KILOGRAM', 40, 'INR', TRUE, 0, FALSE, 'tomato', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'tomato'), 'TOMATO-5KG', 5.000, 'KILOGRAM', 180, 'INR', TRUE, 0, FALSE, 'tomato', 20, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'onion'), 'ONION-1KG', 1.000, 'KILOGRAM', 35, 'INR', TRUE, 0, FALSE, 'onion', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'onion'), 'ONION-5KG', 5.000, 'KILOGRAM', 160, 'INR', TRUE, 0, FALSE, 'onion', 20, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'potato'), 'POTATO-1KG', 1.000, 'KILOGRAM', 30, 'INR', TRUE, 0, FALSE, 'potato', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'potato'), 'POTATO-5KG', 5.000, 'KILOGRAM', 140, 'INR', TRUE, 0, FALSE, 'potato', 20, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'brinjal'), 'BRINJAL-1KG', 1.000, 'KILOGRAM', 45, 'INR', TRUE, 0, FALSE, 'brinjal', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'spinach'), 'SPINACH-1BN', 1.000, 'PACK', 20, 'INR', TRUE, 0, FALSE, 'spinach', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'gongura'), 'GONGURA-1BN', 1.000, 'PACK', 25, 'INR', TRUE, 0, FALSE, 'gongura', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'coriander'), 'CORIANDER-1BN', 1.000, 'PACK', 10, 'INR', TRUE, 0, FALSE, 'coriander', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'banana'), 'BANANA-1DZ', 1.000, 'DOZEN', 60, 'INR', TRUE, 0, FALSE, 'banana', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'mango'), 'MANGO-1KG', 1.000, 'KILOGRAM', 120, 'INR', TRUE, 0, FALSE, 'mango', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'papaya'), 'PAPAYA-1PC', 1.000, 'PIECE', 40, 'INR', TRUE, 0, FALSE, 'papaya', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'milk'), 'MILK-1L', 1.000, 'LITRE', 58, 'INR', TRUE, 0, TRUE, 'milk', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'milk'), 'MILK-500ML', 0.500, 'LITRE', 30, 'INR', TRUE, 0, TRUE, 'milk', 20, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'curd'), 'CURD-500G', 0.500, 'KILOGRAM', 35, 'INR', TRUE, 0, FALSE, 'curd', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'paneer'), 'PANEER-200G', 0.200, 'KILOGRAM', 80, 'INR', TRUE, 500, FALSE, 'paneer', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'rice-sona-masoori'), 'RICE-SM-5KG', 5.000, 'KILOGRAM', 300, 'INR', TRUE, 0, FALSE, 'rice', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'rice-sona-masoori'), 'RICE-SM-25KG', 25.000, 'KILOGRAM', 1450, 'INR', TRUE, 0, TRUE, 'rice', 20, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'toor-dal'), 'TOORDAL-1KG', 1.000, 'KILOGRAM', 140, 'INR', TRUE, 500, FALSE, 'toor-dal', 10, TRUE),
    ((SELECT id FROM catalog_product WHERE code = 'ragi'), 'RAGI-1KG', 1.000, 'KILOGRAM', 70, 'INR', TRUE, 500, FALSE, 'ragi', 10, TRUE);
