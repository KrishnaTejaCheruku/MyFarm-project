package in.myfarm.api.commerce;

import java.util.List;

record OrderPlacedEvent(
		String orderNumber,
		String serviceAreaCode,
		String deliveryWindowCode,
		String paymentMethod,
		long subtotalInr,
		List<Item> items) {

	record Item(
			String sku,
			String nameEn,
			String nameTe,
			int quantity,
			long unitPriceInr) {
	}
}
