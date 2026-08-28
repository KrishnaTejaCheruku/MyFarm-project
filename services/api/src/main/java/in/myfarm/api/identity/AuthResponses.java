package in.myfarm.api.identity;

import com.fasterxml.jackson.annotation.JsonInclude;

public final class AuthResponses {

	private AuthResponses() {
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record OtpRequested(
			String phone,
			int expiresInSeconds,
			// Only populated when myfarm.otp.expose-in-response is true
			// (default in local/dev -- there is no SMS gateway wired up
			// yet, so this is the only way to actually complete the flow
			// today). Must be false anywhere this API is reachable by
			// anyone other than the developer running it.
			String devOtp) {
	}

	public record TokenIssued(
			String accessToken,
			String refreshToken,
			long expiresInSeconds,
			String tokenType) {
	}
}
