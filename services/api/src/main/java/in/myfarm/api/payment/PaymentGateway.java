package in.myfarm.api.payment;

public interface PaymentGateway {

	GatewayOrder createOrder(String orderNumber, long amountInr);

	record GatewayOrder(
			String gatewayOrderId,
			long amountPaise,
			String currency) {
	}
}
