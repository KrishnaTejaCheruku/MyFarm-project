package in.myfarm.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@Sql("/order-test-data.sql")
class OrderApiIntegrationTests {

	private static final String ORDERS_EXCHANGE = "myfarm.orders";
	private static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

	// A customer, identity-wise, is just whoever most recently verified
	// this phone's OTP -- distinct from any phone appearing in
	// commerce_order's own free-text customerPhone field, which is
	// unrelated (order-test-data.sql doesn't seed Keycloak users).
	private static final String TEST_CUSTOMER_PHONE = "9876500001";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	private String customerToken;

	@BeforeEach
	void obtainCustomerToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/otp/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"phone\": \"" + TEST_CUSTOMER_PHONE + "\"}"))
				.andExpect(status().isAccepted());

		// myfarm.otp.expose-in-response defaults true locally/in tests
		// -- there's no SMS gateway to actually deliver this through.
		String code = otpFor(TEST_CUSTOMER_PHONE);

		MvcResult result = mockMvc.perform(post("/api/v1/auth/otp/verify")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"phone\": \"" + TEST_CUSTOMER_PHONE
						+ "\", \"code\": \"" + code + "\"}"))
				.andExpect(status().isOk())
				.andReturn();

		customerToken = JsonPath.read(
				result.getResponse().getContentAsString(), "$.accessToken");
	}

	private String otpFor(String phone) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/otp/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"phone\": \"" + phone + "\"}"))
				.andExpect(status().isAccepted())
				.andReturn();
		return JsonPath.read(
				result.getResponse().getContentAsString(), "$.devOtp");
	}

	@Test
	void placesACodOrderConfirmsItImmediatelyAndPublishesAnEvent()
			throws Exception {
		Queue probeQueue = new AnonymousQueue();
		amqpAdmin.declareQueue(probeQueue);
		amqpAdmin.declareBinding(BindingBuilder
				.bind(probeQueue)
				.to(new TopicExchange(ORDERS_EXCHANGE))
				.with(ORDER_PLACED_ROUTING_KEY));

		MvcResult result = mockMvc.perform(post("/api/v1/orders")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(placeOrderJson("COD")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.orderNumber")
						.value(org.hamcrest.Matchers.matchesPattern(
								"MF-[A-F0-9]{8}")))
				.andExpect(jsonPath("$.status").value("CONFIRMED"))
				.andExpect(jsonPath("$.paymentMethod").value("COD"))
				.andExpect(jsonPath("$.subtotalInr").value(80))
				.andExpect(jsonPath("$.items.length()").value(1))
				.andExpect(jsonPath("$.items[0].sku")
						.value("ORD-TEST-1KG"))
				.andExpect(jsonPath("$.items[0].name.en")
						.value("Test Tomato"))
				.andExpect(jsonPath("$.items[0].quantity").value(2))
				.andExpect(jsonPath("$.items[0].lineTotalInr").value(80))
				.andReturn();

		String orderNumber = JsonPath.read(
				result.getResponse().getContentAsString(), "$.orderNumber");
		String customerSubjectId = JsonPath.read(
				result.getResponse().getContentAsString(),
				"$.customerSubjectId");
		assertThat(customerSubjectId).isNotBlank();

		mockMvc.perform(get("/api/v1/orders/" + orderNumber))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderNumber").value(orderNumber))
				.andExpect(jsonPath("$.customerSubjectId")
						.value(customerSubjectId))
				.andExpect(jsonPath("$.subtotalInr").value(80));

		Message message = rabbitTemplate.receive(probeQueue.getName(), 5000);
		assertThat(message).isNotNull();
		String payload = new String(message.getBody());
		assertThat(payload).contains(orderNumber).contains("ORD-TEST-1KG");
	}

	@Test
	void placesAnOnlineOrderAsPendingPayment() throws Exception {
		mockMvc.perform(post("/api/v1/orders")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(placeOrderJson("ONLINE_UPI")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
				.andExpect(jsonPath("$.paymentMethod").value("ONLINE_UPI"));
	}

	@Test
	void rejectsOrderPlacementWithoutAuthentication() throws Exception {
		mockMvc.perform(post("/api/v1/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(placeOrderJson("COD")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsOrdersForAnInactiveServiceArea() throws Exception {
		String body = """
				{
				  "serviceAreaCode": "orders-test-inactive-area",
				  "deliveryWindowCode": "morning",
				  "customerName": "Asha Rao",
				  "customerPhone": "9876543210",
				  "deliveryAddressLine1": "12-3 Beach Road",
				  "deliveryPincode": "530001",
				  "paymentMethod": "COD",
				  "items": [ { "sku": "ORD-TEST-1KG", "quantity": 1 } ]
				}
				""";

		mockMvc.perform(post("/api/v1/orders")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.type")
						.value("urn:myfarm:problem:invalid-order"));
	}

	@Test
	void rejectsOrdersForAnInactiveDeliveryWindow() throws Exception {
		String body = """
				{
				  "serviceAreaCode": "orders-test-area",
				  "deliveryWindowCode": "inactive-window",
				  "customerName": "Asha Rao",
				  "customerPhone": "9876543210",
				  "deliveryAddressLine1": "12-3 Beach Road",
				  "deliveryPincode": "530001",
				  "paymentMethod": "COD",
				  "items": [ { "sku": "ORD-TEST-1KG", "quantity": 1 } ]
				}
				""";

		mockMvc.perform(post("/api/v1/orders")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void rejectsOrdersForAnUnknownOrInactiveVariant() throws Exception {
		String body = """
				{
				  "serviceAreaCode": "orders-test-area",
				  "deliveryWindowCode": "morning",
				  "customerName": "Asha Rao",
				  "customerPhone": "9876543210",
				  "deliveryAddressLine1": "12-3 Beach Road",
				  "deliveryPincode": "530001",
				  "paymentMethod": "COD",
				  "items": [ { "sku": "ORD-TEST-INACTIVE", "quantity": 1 } ]
				}
				""";

		mockMvc.perform(post("/api/v1/orders")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void rejectsMalformedPhoneNumbers() throws Exception {
		String body = """
				{
				  "serviceAreaCode": "orders-test-area",
				  "deliveryWindowCode": "morning",
				  "customerName": "Asha Rao",
				  "customerPhone": "12345",
				  "deliveryAddressLine1": "12-3 Beach Road",
				  "deliveryPincode": "530001",
				  "paymentMethod": "COD",
				  "items": [ { "sku": "ORD-TEST-1KG", "quantity": 1 } ]
				}
				""";

		mockMvc.perform(post("/api/v1/orders")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returnsNotFoundForAnUnknownOrderNumber() throws Exception {
		mockMvc.perform(get("/api/v1/orders/MF-00000000"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.type")
						.value("urn:myfarm:problem:order-not-found"));
	}

	private static String placeOrderJson(String paymentMethod) {
		return """
				{
				  "serviceAreaCode": "orders-test-area",
				  "deliveryWindowCode": "morning",
				  "customerName": "Asha Rao",
				  "customerPhone": "9876543210",
				  "deliveryAddressLine1": "12-3 Beach Road",
				  "deliveryPincode": "530001",
				  "paymentMethod": "%s",
				  "items": [ { "sku": "ORD-TEST-1KG", "quantity": 2 } ]
				}
				""".formatted(paymentMethod);
	}
}
