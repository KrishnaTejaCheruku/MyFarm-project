package in.myfarm.api.commerce;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = OrderController.class)
class OrderExceptionHandler {

	private static final URI ORDER_NOT_FOUND = URI.create(
			"urn:myfarm:problem:order-not-found");
	private static final URI INVALID_ORDER = URI.create(
			"urn:myfarm:problem:invalid-order");

	@ExceptionHandler(OrderNotFoundException.class)
	ResponseEntity<ProblemDetail> orderNotFound(
			OrderNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setType(ORDER_NOT_FOUND);
		problem.setTitle("Order not found");
		problem.setProperty("orderNumber", exception.orderNumber());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}

	@ExceptionHandler(InvalidOrderException.class)
	ResponseEntity<ProblemDetail> invalidOrder(
			InvalidOrderException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
		problem.setType(INVALID_ORDER);
		problem.setTitle("Order could not be placed");
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
				.body(problem);
	}
}
