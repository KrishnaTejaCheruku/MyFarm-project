package in.myfarm.api.catalog;

// Wraps a checked IOException from the OpenSearch client (connection
// refused, timeout, etc.) so it can flow through the same
// @RestControllerAdvice problem-detail pattern as every other catalog
// exception instead of surfacing as a bare 500.
final class CatalogSearchUnavailableException extends RuntimeException {

	CatalogSearchUnavailableException(Throwable cause) {
		super("Product search is temporarily unavailable", cause);
	}
}
