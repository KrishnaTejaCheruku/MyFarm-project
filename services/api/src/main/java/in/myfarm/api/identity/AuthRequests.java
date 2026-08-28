package in.myfarm.api.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class AuthRequests {

	private static final String PHONE_PATTERN = "[6-9][0-9]{9}";
	private static final String OTP_PATTERN = "[0-9]{6}";

	private AuthRequests() {
	}

	public record RequestOtp(
			@NotBlank @Pattern(regexp = PHONE_PATTERN) String phone) {
	}

	public record VerifyOtp(
			@NotBlank @Pattern(regexp = PHONE_PATTERN) String phone,
			@NotBlank @Pattern(regexp = OTP_PATTERN) String code) {
	}
}
