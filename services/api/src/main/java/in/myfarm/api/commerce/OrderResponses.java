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

	public record Order(
			String orderNumber,
			String serviceAreaCode,
			String deliveryWindowCode,
			String customerSubjectId,
			String status,
			String paymentMethod,
			long subtotalInr,
			List<OrderItem> items) {

		public Order {
			items = List.copyOf(items);
		}
	}
}
