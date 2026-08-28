package in.myfarm.api.delivery;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ServiceAreaController.class)
class DeliveryOptionExceptionHandler {

	private static final URI OPTION_NOT_FOUND = URI.create(
			"urn:myfarm:problem:delivery-option-not-found");

	@ExceptionHandler(DeliveryOptionNotFoundException.class)
	ResponseEntity<ProblemDetail> optionNotFound(
			DeliveryOptionNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setType(OPTION_NOT_FOUND);
		problem.setTitle("Delivery option not found");
		problem.setProperty("option", exception.option());
		problem.setProperty("code", exception.code());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}
}
