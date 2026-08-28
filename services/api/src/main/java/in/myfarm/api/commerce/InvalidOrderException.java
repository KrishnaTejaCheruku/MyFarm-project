package in.myfarm.api.commerce;

/**
 * A business-rule violation on order placement (inactive service area,
 * inactive delivery window, unknown/inactive variant) -- as opposed to
 * a request-shape violation, which bean validation already rejects
 * with 400 before this ever runs.
 */
final class InvalidOrderException extends RuntimeException {

	InvalidOrderException(String reason) {
		super(reason);
	}
}
