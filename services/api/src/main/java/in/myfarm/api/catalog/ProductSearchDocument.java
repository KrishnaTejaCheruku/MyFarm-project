package in.myfarm.api.catalog;

// Denormalized OpenSearch document shape for CatalogSearchIndexService/
// CatalogSearchQueryService. Deliberately doesn't carry variants/pricing
// -- those change independently of name/description and a search hit
// only needs enough to link to the full product (via slug), which the
// existing GET /api/v1/catalog/products/{slug} already serves.
record ProductSearchDocument(
		String code,
		String slug,
		String categoryCode,
		String nameEn,
		String nameTe,
		String descriptionEn,
		String descriptionTe) {
}
