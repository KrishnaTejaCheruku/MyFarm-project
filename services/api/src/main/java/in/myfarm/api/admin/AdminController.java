package in.myfarm.api.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal placeholder proving the admin+TOTP identity path actually
 * protects something end to end (SecurityConfiguration requires the
 * "admin" realm role for all of /api/v1/admin/**). Real back-office
 * endpoints (catalog management, order operations, etc.) are future
 * work, not part of the identity phase.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

	@GetMapping("/whoami")
	public AdminResponses.WhoAmI whoAmI(@AuthenticationPrincipal Jwt jwt) {
		return new AdminResponses.WhoAmI(
				jwt.getSubject(), jwt.getClaimAsString("preferred_username"));
	}
}
