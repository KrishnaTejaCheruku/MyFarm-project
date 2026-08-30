package in.myfarm.api.payment;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import in.myfarm.api.commerce.OrderPayments;

/**
 * Plays the role a real gateway's webhook would play: something
 * external telling us a payment settled or failed, independent of
 * whatever the storefront's own UI thinks happened. In production this
 * would be Razorpay/Stripe calling an HTTPS endpoint with an HMAC
 * signature; here it's a plain endpoint you call directly while
 * testing --
 * curl -X POST http://localhost:8082/api/v1/payments/mock/{gatewayOrderId}/succeed
 *
 * This is the piece that gets deleted (not adapted) the day a real
 * gateway is wired in -- everything else in this feature survives that
 * swap unchanged.
 */
@RestController
@RequestMapping("/api/v1/payments/mock")
class MockPaymentSimulatorController {

	private final OrderPayments orderPayments;

	MockPaymentSimulatorController(OrderPayments orderPayments) {
		this.orderPayments = orderPayments;
	}

	@PostMapping("/{gatewayOrderId}/succeed")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void simulateSuccess(@PathVariable String gatewayOrderId) {
		orderPayments.markPaid(gatewayOrderId, "mock_payment_" + UUID.randomUUID());
	}

	@PostMapping("/{gatewayOrderId}/fail")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	void simulateFailure(@PathVariable String gatewayOrderId) {
		orderPayments.markPaymentFailed(gatewayOrderId);
	}
}
