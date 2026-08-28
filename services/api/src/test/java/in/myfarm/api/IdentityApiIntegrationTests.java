package in.myfarm.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Covers the OTP -> Keycloak token bridge (AuthController,
 * KeycloakIdentityBridge) and the resulting authorization wiring on
 * commerce/admin endpoints. Runs against a real Keycloak instance
 * (Testcontainers, see TestcontainersConfiguration) importing the
 * exact realm dev uses -- infra/identity/myfarm-realm.json.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class IdentityApiIntegrationTests {

	private static final String CUSTOMER_PHONE = "9876500002";
	private static final String WRONG_ATTEMPTS_PHONE = "9876500003";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RestClient.Builder restClientBuilder;

	@Value("${myfarm.keycloak.base-url}")
	private String keycloakBaseUrl;

	@Test
	void requestingAnOtpReturnsTheDevCodeInTestProfile() throws Exception {
		mockMvc.perform(post("/api/v1/auth/otp/request")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phone\": \"" + CUSTOMER_PHONE + "\"}"))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.phone").value(CUSTOMER_PHONE))
				.andExpect(jsonPath("$.devOtp").isNotEmpty());
	}

	@Test
	void verifyingTheCorrectOtpIssuesARealKeycloakToken() throws Exception {
		String code = requestOtp(CUSTOMER_PHONE);

		MvcResult result = mockMvc.perform(post("/api/v1/auth/otp/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phone\": \"" + CUSTOMER_PHONE
								+ "\", \"code\": \"" + code + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn();

		String token = JsonPath.read(
				result.getResponse().getContentAsString(), "$.accessToken");
		assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
	}

	@Test
	void rejectsAWrongOtpCode() throws Exception {
		requestOtp(CUSTOMER_PHONE);

		mockMvc.perform(post("/api/v1/auth/otp/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phone\": \"" + CUSTOMER_PHONE
								+ "\", \"code\": \"000000\"}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.type")
						.value("urn:myfarm:problem:invalid-otp"));
	}

	@Test
	void locksOutAfterTooManyWrongAttempts() throws Exception {
		String code = requestOtp(WRONG_ATTEMPTS_PHONE);

		for (int i = 0; i < 5; i++) {
			mockMvc.perform(post("/api/v1/auth/otp/verify")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"phone\": \"" + WRONG_ATTEMPTS_PHONE
									+ "\", \"code\": \"000000\"}"))
					.andExpect(status().isUnprocessableEntity());
		}

		// Even the correct code is now rejected -- the challenge is spent.
		mockMvc.perform(post("/api/v1/auth/otp/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phone\": \"" + WRONG_ATTEMPTS_PHONE
								+ "\", \"code\": \"" + code + "\"}"))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void adminWhoAmIRejectsUnauthenticatedRequests() throws Exception {
		mockMvc.perform(get("/api/v1/admin/whoami"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminWhoAmIRejectsACustomerToken() throws Exception {
		String code = requestOtp("9876500004");
		MvcResult result = mockMvc.perform(post("/api/v1/auth/otp/verify")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phone\": \"9876500004\", \"code\": \""
								+ code + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		String customerToken = JsonPath.read(
				result.getResponse().getContentAsString(), "$.accessToken");

		mockMvc.perform(get("/api/v1/admin/whoami")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminWhoAmIAcceptsTheQaAdminAccount() throws Exception {
		// qa-admin is a realm-seeded, no-required-actions fixture used
		// only by tests -- Keycloak's password grant refuses to issue a
		// token for an account with pending required actions, which the
		// real human "admin" seed user always has until they complete
		// TOTP enrollment interactively (see myfarm-realm.json).
		String adminToken = passwordGrantToken("qa-admin", "qa-admin-test-password");

		mockMvc.perform(get("/api/v1/admin/whoami")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("qa-admin"));
	}

	private String requestOtp(String phone) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/otp/request")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phone\": \"" + phone + "\"}"))
				.andExpect(status().isAccepted())
				.andReturn();
		return JsonPath.read(
				result.getResponse().getContentAsString(), "$.devOtp");
	}

	private String passwordGrantToken(String username, String password) {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "password");
		form.add("client_id", "identity-bridge");
		form.add("client_secret", "myfarm-identity-bridge-secret");
		form.add("username", username);
		form.add("password", password);

		Map<String, Object> response = restClientBuilder.build().post()
				.uri(keycloakBaseUrl + "/realms/myfarm/protocol/openid-connect/token")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(new org.springframework.core.ParameterizedTypeReference<
						LinkedHashMap<String, Object>>() {
				});

		return String.valueOf(response.get("access_token"));
	}
}
