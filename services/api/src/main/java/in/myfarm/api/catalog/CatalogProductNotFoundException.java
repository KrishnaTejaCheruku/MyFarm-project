package in.myfarm.api.catalog;

final class CatalogProductNotFoundException extends RuntimeException {

	private final String slug;

	CatalogProductNotFoundException(String slug) {
		super("No active catalogue product has slug '%s'".formatted(slug));
		this.slug = slug;
	}

	String slug() {
		return slug;
	}
}
