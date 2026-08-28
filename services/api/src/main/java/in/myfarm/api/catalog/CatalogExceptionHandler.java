package in.myfarm.api.catalog;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CatalogController.class)
class CatalogExceptionHandler {

	private static final URI PRODUCT_NOT_FOUND = URI.create(
			"urn:myfarm:problem:catalog-product-not-found");

	@ExceptionHandler(CatalogProductNotFoundException.class)
	ResponseEntity<ProblemDetail> productNotFound(
			CatalogProductNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.NOT_FOUND, exception.getMessage());
		problem.setType(PRODUCT_NOT_FOUND);
		problem.setTitle("Catalogue product not found");
		problem.setProperty("slug", exception.slug());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
	}
}
