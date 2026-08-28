-- Ties each order to the Keycloak subject (JWT "sub" claim) that
-- placed it, now that identity/OTP (phase 2) requires customers to be
-- authenticated before ordering -- see SecurityConfiguration's
-- POST /api/v1/orders matcher.
ALTER TABLE commerce_order
    ADD COLUMN customer_subject_id VARCHAR(36) NOT NULL
        AFTER customer_phone;

CREATE INDEX idx_commerce_order_subject
    ON commerce_order (customer_subject_id);
