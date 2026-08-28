package in.myfarm.api.identity;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/otp")
public class AuthController {

	private static final int CHALLENGE_TTL_SECONDS = 300;

	private final OtpService otpService;
	private final KeycloakIdentityBridge identityBridge;
	private final boolean exposeOtpInResponse;

	AuthController(
			OtpService otpService,
			KeycloakIdentityBridge identityBridge,
			@Value("${myfarm.otp.expose-in-response:false}")
			boolean exposeOtpInResponse) {
		this.otpService = otpService;
		this.identityBridge = identityBridge;
		this.exposeOtpInResponse = exposeOtpInResponse;
	}

	@PostMapping("/request")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public AuthResponses.OtpRequested requestOtp(
			@Valid @RequestBody AuthRequests.RequestOtp request) {
		String code = otpService.requestOtp(request.phone());
		return new AuthResponses.OtpRequested(
				request.phone(),
				CHALLENGE_TTL_SECONDS,
				exposeOtpInResponse ? code : null);
	}

	@PostMapping("/verify")
	public AuthResponses.TokenIssued verifyOtp(
			@Valid @RequestBody AuthRequests.VerifyOtp request) {
		otpService.verifyOtp(request.phone(), request.code());
		return identityBridge.issueTokenForPhone(request.phone());
	}
}
