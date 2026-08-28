package in.myfarm.api.commerce;

final class OrderNotFoundException extends RuntimeException {

	private final String orderNumber;

	OrderNotFoundException(String orderNumber) {
		super("No order found with number '%s'".formatted(orderNumber));
		this.orderNumber = orderNumber;
	}

	String orderNumber() {
		return orderNumber;
	}
}
