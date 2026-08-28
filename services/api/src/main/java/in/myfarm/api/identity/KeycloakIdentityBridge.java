package in.myfarm.api.identity;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Bridges phone-OTP verification (handled entirely in OtpService --
 * Keycloak has no built-in SMS OTP flow) to a real Keycloak-issued
 * token. Approach: find-or-create the customer as a Keycloak user
 * keyed by phone number, set a random one-time password via the Admin
 * REST API, then immediately exchange it for a token via the standard
 * password grant. The random password is never returned to a caller
 * -- Keycloak just remembers it as that user's current credential
 * until the next OTP-verified login rotates it again.
 *
 * All calls here run server-side, authenticated as the identity-bridge
 * confidential client's own service account (client_credentials grant,
 * granted realm-management's manage-users/view-users/query-users roles
 * -- see infra/identity/myfarm-realm.json). Nothing in this class is
 * reachable by a browser directly.
 */
@Component
class KeycloakIdentityBridge {

	private static final ParameterizedTypeReference<List<Map<String, Object>>> USER_LIST =
			new ParameterizedTypeReference<>() {
			};
	private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
			new ParameterizedTypeReference<>() {
			};

	private final RestClient restClient;
	private final Clock clock;
	private final String realm;
	private final String bridgeClientId;
	private final String bridgeClientSecret;

	private volatile CachedToken serviceAccountToken;

	KeycloakIdentityBridge(
			RestClient.Builder restClientBuilder,
			Clock clock,
			@Value("${myfarm.keycloak.base-url}") String baseUrl,
			@Value("${myfarm.keycloak.realm}") String realm,
			@Value("${myfarm.keycloak.bridge-client-id}") String bridgeClientId,
			@Value("${myfarm.keycloak.bridge-client-secret}") String bridgeClientSecret) {
		this.restClient = restClientBuilder.baseUrl(baseUrl).build();
		this.clock = clock;
		this.realm = realm;
		this.bridgeClientId = bridgeClientId;
		this.bridgeClientSecret = bridgeClientSecret;
	}

	AuthResponses.TokenIssued issueTokenForPhone(String phone) {
		String userId = findOrCreateCustomer(phone);
		String randomPassword = UUID.randomUUID().toString();
		resetPassword(userId, randomPassword);
		return passwordGrant(phone, randomPassword);
	}

	private String findOrCreateCustomer(String phone) {
		List<Map<String, Object>> found = restClient.get()
				.uri("/admin/realms/{realm}/users?username={phone}&exact=true",
						realm, phone)
				.headers(this::withServiceAccountAuth)
				.retrieve()
				.body(USER_LIST);

		if (found != null && !found.isEmpty()) {
			return String.valueOf(found.get(0).get("id"));
		}

		Map<String, Object> newUser = Map.of(
				"username", phone,
				"enabled", true,
				"attributes", Map.of("phoneNumber", List.of(phone)));

		ResponseEntity<Void> response = restClient.post()
				.uri("/admin/realms/{realm}/users", realm)
				.headers(this::withServiceAccountAuth)
				.contentType(MediaType.APPLICATION_JSON)
				.body(newUser)
				.retrieve()
				.toBodilessEntity();

		String location = response.getHeaders().getFirst(HttpHeaders.LOCATION);
		if (location == null) {
			throw new IllegalStateException(
					"Keycloak did not return a Location header for the new user");
		}
		String userId = location.substring(location.lastIndexOf('/') + 1);

		assignCustomerRole(userId);
		return userId;
	}

	private void assignCustomerRole(String userId) {
		Map<String, Object> role = restClient.get()
				.uri("/admin/realms/{realm}/roles/customer", realm)
				.headers(this::withServiceAccountAuth)
				.retrieve()
				.body(JSON_OBJECT);

		restClient.post()
				.uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm",
						realm, userId)
				.headers(this::withServiceAccountAuth)
				.contentType(MediaType.APPLICATION_JSON)
				.body(List.of(role))
				.retrieve()
				.toBodilessEntity();
	}

	private void resetPassword(String userId, String password) {
		Map<String, Object> credential = Map.of(
				"type", "password",
				"value", password,
				"temporary", false);

		restClient.put()
				.uri("/admin/realms/{realm}/users/{userId}/reset-password",
						realm, userId)
				.headers(this::withServiceAccountAuth)
				.contentType(MediaType.APPLICATION_JSON)
				.body(credential)
				.retrieve()
				.toBodilessEntity();
	}

	private AuthResponses.TokenIssued passwordGrant(String phone, String password) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "password");
		form.add("client_id", bridgeClientId);
		form.add("client_secret", bridgeClientSecret);
		form.add("username", phone);
		form.add("password", password);

		Map<String, Object> token = restClient.post()
				.uri("/realms/{realm}/protocol/openid-connect/token", realm)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(JSON_OBJECT);

		return new AuthResponses.TokenIssued(
				String.valueOf(token.get("access_token")),
				String.valueOf(token.get("refresh_token")),
				((Number) token.get("expires_in")).longValue(),
				String.valueOf(token.get("token_type")));
	}

	private void withServiceAccountAuth(HttpHeaders headers) {
		headers.setBearerAuth(serviceAccountToken());
	}

	private String serviceAccountToken() {
		CachedToken cached = serviceAccountToken;
		if (cached != null && cached.expiresAt().isAfter(clock.instant())) {
			return cached.value();
		}
		synchronized (this) {
			cached = serviceAccountToken;
			if (cached != null && cached.expiresAt().isAfter(clock.instant())) {
				return cached.value();
			}

			MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
			form.add("grant_type", "client_credentials");
			form.add("client_id", bridgeClientId);
			form.add("client_secret", bridgeClientSecret);

			Map<String, Object> token = restClient.post()
					.uri("/realms/{realm}/protocol/openid-connect/token", realm)
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(form)
					.retrieve()
					.body(JSON_OBJECT);

			String value = String.valueOf(token.get("access_token"));
			long expiresIn = ((Number) token.get("expires_in")).longValue();
			// Refresh a little early rather than racing the exact expiry.
			Instant expiresAt = clock.instant().plusSeconds(Math.max(expiresIn - 10, 0));
			serviceAccountToken = new CachedToken(value, expiresAt);
			return value;
		}
	}

	private record CachedToken(String value, Instant expiresAt) {
	}
}
