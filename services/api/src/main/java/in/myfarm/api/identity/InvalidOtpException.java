package in.myfarm.api.identity;

class InvalidOtpException extends RuntimeException {

	InvalidOtpException() {
		super("Invalid, expired, or already-used OTP");
	}
}
