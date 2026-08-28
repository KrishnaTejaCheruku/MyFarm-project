package in.myfarm.api.catalog;

/**
 * Read-only, price-and-name-at-this-moment view of a variant, for callers
 * outside the catalog package (e.g. commerce, when it snapshots what a
 * customer ordered). Deliberately not the entity itself -- entities stay
 * package-private so other modules can't accidentally hold a live,
 * lazily-loaded reference across a transaction boundary.
 */
public record VariantSnapshot(
		Long id,
		String sku,
		String nameEn,
		String nameTe,
		long priceInr) {
}
