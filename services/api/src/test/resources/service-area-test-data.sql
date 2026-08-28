DELETE FROM delivery_subscription_plan;
DELETE FROM delivery_window;
DELETE FROM delivery_service_area_pincode;
DELETE FROM delivery_service_area;

INSERT INTO delivery_service_area (
    id, code, name_en, name_te, city, state, timezone,
    subscription_required, active, version
) VALUES
    (
        4001, 'seethammadhara', 'Seethammadhara', 'సీతమ్మధార',
        'Visakhapatnam', 'Andhra Pradesh', 'Asia/Kolkata',
        TRUE, TRUE, 0
    ),
    (
        4002, 'inactive-area', 'Inactive Area', NULL,
        'Visakhapatnam', 'Andhra Pradesh', 'Asia/Kolkata',
        TRUE, FALSE, 0
    );

INSERT INTO delivery_service_area_pincode (
    id, service_area_id, pincode, active, version
) VALUES
    (5001, 4001, '530013', TRUE, 0);
