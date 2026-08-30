-- Phase 3 (payments). gateway_order_id/gateway_payment_id/paid_at
-- track a payment gateway's own order (currently the mock gateway --
-- see the payment package -- a real one later, e.g. Razorpay, would
-- populate these the same way). All three are NULL-able: COD orders
-- (and later, approved-monthly-billing orders) never get a gateway
-- order at all. A single gateway_order_id per commerce order is
-- enough because a gateway order accepts multiple payment attempts
-- (retries) against the same id until one succeeds -- there's no need
-- for a separate payments table modeling something the gateway
-- already tracks itself.
ALTER TABLE commerce_order
    ADD COLUMN gateway_order_id VARCHAR(64) NULL AFTER payment_method,
    ADD COLUMN gateway_payment_id VARCHAR(64) NULL AFTER gateway_order_id,
    ADD COLUMN paid_at DATETIME(6) NULL AFTER gateway_payment_id,
    ADD CONSTRAINT uk_commerce_order_gateway_order_id UNIQUE (gateway_order_id);
