package in.myfarm.api.commerce;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
class OrderEventPublisher {

	private final RabbitTemplate rabbitTemplate;

	OrderEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	void publishOrderPlaced(OrderEntity order) {
		OrderPlacedEvent event = new OrderPlacedEvent(
				order.orderNumber(),
				order.serviceAreaCode(),
				order.deliveryWindowCode(),
				order.paymentMethod().name(),
				order.subtotalInr(),
				order.items().stream()
						.map(item -> new OrderPlacedEvent.Item(
								item.sku(),
								item.nameEn(),
								item.nameTe(),
								item.quantity(),
								item.unitPriceInr()))
						.toList());
		rabbitTemplate.convertAndSend(
				CommerceMessagingConfiguration.ORDERS_EXCHANGE,
				CommerceMessagingConfiguration.ORDER_PLACED_ROUTING_KEY,
				event);
	}
}
