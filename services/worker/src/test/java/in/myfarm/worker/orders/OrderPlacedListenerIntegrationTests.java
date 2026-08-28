package in.myfarm.worker.orders;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import in.myfarm.worker.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OrderPlacedListenerIntegrationTests {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private OrderPlacedListener listener;

	@Test
	void consumesAnOrderPlacedEventPublishedByTheApi() throws Exception {
		Map<String, Object> event = Map.of(
				"orderNumber", "MF-TESTEVT1",
				"serviceAreaCode", "orders-test-area",
				"deliveryWindowCode", "morning",
				"paymentMethod", "COD",
				"subtotalInr", 80,
				"items", List.of(Map.of(
						"sku", "ORD-TEST-1KG",
						"nameEn", "Test Tomato",
						"nameTe", "టెస్ట్ టమాటో",
						"quantity", 2,
						"unitPriceInr", 40)));

		rabbitTemplate.convertAndSend(
				OrderEventsMessagingConfiguration.ORDERS_EXCHANGE,
				OrderEventsMessagingConfiguration.ORDER_PLACED_ROUTING_KEY,
				event);

		Map<String, Object> received = null;
		for (int attempt = 0; attempt < 20 && received == null; attempt++) {
			Thread.sleep(250);
			received = listener.lastEvent.get();
		}

		assertThat(received).isNotNull();
		assertThat(received.get("orderNumber")).isEqualTo("MF-TESTEVT1");
	}
}
