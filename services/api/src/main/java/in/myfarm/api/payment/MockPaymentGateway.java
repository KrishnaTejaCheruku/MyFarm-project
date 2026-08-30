package in.myfarm.api.payment;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Stands in for a real gateway (Razorpay, Stripe, whatever) so this
 * project can demonstrate the production pattern -- create a gateway
 * order, wait for an async confirmation, never trust the client alone
 * -- without an external account. See MockPaymentSimulatorController
 * for the "gateway calls us back" half of the pattern.
 *
 * Swapping this for a real gateway later means writing one class that
 * implements PaymentGateway and pointing Spring at it instead -- every
 * other file in this feature (OrderService, OrderEntity, OrderPayments,
 * the whole order state machine) stays exactly as it is.
 */
@Component
class MockPaymentGateway implements PaymentGateway {

	@Override
	public GatewayOrder createOrder(String orderNumber, long amountInr) {
		String gatewayOrderId = "mock_order_" + UUID.randomUUID();
		return new GatewayOrder(gatewayOrderId, amountInr * 100, "INR");
	}
}
