package in.myfarm.api.commerce;

import java.util.List;

public final class OrderResponses {

	private OrderResponses() {
	}

	public record LocalizedText(String en, String te) {
	}

	public record OrderItem(
			String sku,
			LocalizedText name,
			int quantity,
			long unitPriceInr,
			long lineTotalInr) {
	}

	// Present only for gateway-backed payment methods (ONLINE_UPI) --
	// null for COD. Carries what the storefront needs to continue a
	// payment against the gateway (today, the mock gateway -- see the
	// payment package).
	public record Payment(
			String gatewayOrderId,
			long amountPaise,
			String currency) {
	}

	public record Order(
			String orderNumber,
			String serviceAreaCode,
			String deliveryWindowCode,
			String customerSubjectId,
			String status,
			String paymentMethod,
			long subtotalInr,
			Payment payment,
			List<OrderItem> items) {

		public Order {
			items = List.copyOf(items);
		}
	}
}
