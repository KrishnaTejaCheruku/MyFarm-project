package in.myfarm.api.catalog;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public final class CatalogResponses {

	private CatalogResponses() {
	}

	public record LocalizedText(String en, String te) implements Serializable {
	}

	public record Category(
			String code,
			String slug,
			LocalizedText name) implements Serializable {
	}

	public record Money(
			String currency,
			long amountInr,
			boolean taxInclusive) implements Serializable {
	}

	public record Variant(
			String sku,
			BigDecimal quantity,
			String unit,
			Money price,
			int gstBasisPoints,
			boolean subscriptionAllowed,
			String imageKey) implements Serializable {
	}

	public record Product(
			String code,
			String slug,
			String categoryCode,
			LocalizedText name,
			LocalizedText description,
			List<Variant> variants) implements Serializable {

		public Product {
			variants = List.copyOf(variants);
		}
	}

	public record Page<T>(
			List<T> items,
			int page,
			int size,
			long totalElements,
			int totalPages) implements Serializable {

		public Page {
			items = List.copyOf(items);
		}
	}

	// Deliberately lighter than Product -- no variants/pricing. See
	// ProductSearchDocument for why: a search hit only needs enough to
	// link to the full product via slug.
	public record SearchHit(
			String code,
			String slug,
			String categoryCode,
			LocalizedText name) implements Serializable {
	}
}
