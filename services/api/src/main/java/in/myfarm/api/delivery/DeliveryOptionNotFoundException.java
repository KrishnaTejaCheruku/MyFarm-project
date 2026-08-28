package in.myfarm.api.delivery;

final class DeliveryOptionNotFoundException extends RuntimeException {

	private final String option;
	private final String code;

	DeliveryOptionNotFoundException(String option, String code) {
		super("No active %s has code '%s'".formatted(option, code));
		this.option = option;
		this.code = code;
	}

	String option() {
		return option;
	}

	String code() {
		return code;
	}
}
