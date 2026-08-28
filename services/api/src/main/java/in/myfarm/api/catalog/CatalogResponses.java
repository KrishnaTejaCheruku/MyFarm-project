package in.myfarm.api.catalog;

import java.math.BigDecimal;
import java.util.List;

public final class CatalogResponses {

	private CatalogResponses() {
	}

	public record LocalizedText(String en, String te) {
	}

	public record Category(
			String code,
			String slug,
			LocalizedText name) {
	}

	public record Money(
			String currency,
			long amountInr,
			boolean taxInclusive) {
	}

	public record Variant(
			String sku,
			BigDecimal quantity,
			String unit,
			Money price,
			int gstBasisPoints,
			boolean subscriptionAllowed,
			String imageKey) {
	}

	public record Product(
			String code,
			String slug,
			String categoryCode,
			LocalizedText name,
			LocalizedText description,
			List<Variant> variants) {

		public Product {
			variants = List.copyOf(variants);
		}
	}

	public record Page<T>(
			List<T> items,
			int page,
			int size,
			long totalElements,
			int totalPages) {

		public Page {
			items = List.copyOf(items);
		}
	}
}
