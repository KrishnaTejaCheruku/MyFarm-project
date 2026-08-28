DELETE FROM delivery_subscription_plan;
DELETE FROM delivery_window;
DELETE FROM delivery_service_area_pincode;
DELETE FROM delivery_service_area;

INSERT INTO delivery_service_area (
    id, code, name_en, name_te, city, state, timezone,
    subscription_required, active, version
) VALUES
    (
        6001, 'seethammadhara', 'Seethammadhara', 'సీతమ్మధార',
        'Visakhapatnam', 'Andhra Pradesh', 'Asia/Kolkata',
        TRUE, TRUE, 0
    ),
    (
        6002, 'inactive-area', 'Inactive Area', NULL,
        'Visakhapatnam', 'Andhra Pradesh', 'Asia/Kolkata',
        TRUE, FALSE, 0
    );

INSERT INTO delivery_window (
    id, service_area_id, code, name_en, name_te, starts_at, ends_at,
    cutoff_minutes_before, sort_order, active, version
) VALUES
    (
        7001, 6001, 'morning', 'Morning', 'ఉదయం', '05:30:00', '08:30:00',
        480, 10, TRUE, 0
    ),
    (
        7002, 6001, 'inactive-window', 'Inactive', NULL,
        '09:00:00', '10:00:00', 60, 20, FALSE, 0
    );

INSERT INTO delivery_subscription_plan (
    id, service_area_id, code, name_en, name_te, billing_period,
    duration_months, sort_order, active, version
) VALUES
    (
        8001, 6001, 'monthly', 'Monthly', 'నెలవారీ',
        'MONTHLY', 1, 10, TRUE, 0
    ),
    (
        8002, 6001, 'yearly', 'Yearly', 'వార్షిక',
        'YEARLY', 12, 20, TRUE, 0
    ),
    (
        8003, 6001, 'inactive-plan', 'Inactive', NULL,
        'MONTHLY', 1, 30, FALSE, 0
    );
