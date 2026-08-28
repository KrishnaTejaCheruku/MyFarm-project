package in.myfarm.api.identity;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
class AuthExceptionHandler {

	@ExceptionHandler(InvalidOtpException.class)
	ProblemDetail handleInvalidOtp(InvalidOtpException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
		problem.setType(URI.create("urn:myfarm:problem:invalid-otp"));
		return problem;
	}
}
