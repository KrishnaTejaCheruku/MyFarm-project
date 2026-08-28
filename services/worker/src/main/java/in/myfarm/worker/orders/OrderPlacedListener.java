package in.myfarm.worker.orders;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * First real consumer of an api-published event -- proves the
 * publish/consume pipe works end to end. Deliberately does nothing
 * beyond logging for now; the actual fulfillment/notification logic
 * is follow-up work once there's a product decision on what worker
 * should do with a placed order.
 */
@Component
class OrderPlacedListener {

	private static final Logger log =
			LoggerFactory.getLogger(OrderPlacedListener.class);

	// Package-visible for OrderPlacedListenerIntegrationTests to poll --
	// not a real API, just the simplest way to assert delivery without
	// pulling in an extra test dependency for async waiting.
	final AtomicReference<Map<String, Object>> lastEvent =
			new AtomicReference<>();

	@RabbitListener(queues = OrderEventsMessagingConfiguration.ORDER_PLACED_QUEUE)
	void onOrderPlaced(Map<String, Object> event) {
		log.info(
				"Order placed: orderNumber={} serviceAreaCode={} subtotalInr={} items={}",
				event.get("orderNumber"),
				event.get("serviceAreaCode"),
				event.get("subtotalInr"),
				event.get("items"));
		lastEvent.set(event);
	}
}
