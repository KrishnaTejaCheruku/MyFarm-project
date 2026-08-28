package in.myfarm.worker.orders;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Matches services/api's CommerceMessagingConfiguration -- same
 * exchange name and routing key, declared idempotently on both sides
 * so either service can start first.
 */
@Configuration(proxyBeanMethods = false)
class OrderEventsMessagingConfiguration {

	static final String ORDERS_EXCHANGE = "myfarm.orders";
	static final String ORDER_PLACED_ROUTING_KEY = "order.placed";
	static final String ORDER_PLACED_QUEUE = "myfarm.orders.worker";

	@Bean
	TopicExchange ordersExchange() {
		return new TopicExchange(ORDERS_EXCHANGE, true, false);
	}

	@Bean
	Queue orderPlacedQueue() {
		return new Queue(ORDER_PLACED_QUEUE, true);
	}

	@Bean
	Binding orderPlacedBinding(
			Queue orderPlacedQueue, TopicExchange ordersExchange) {
		return BindingBuilder.bind(orderPlacedQueue)
				.to(ordersExchange)
				.with(ORDER_PLACED_ROUTING_KEY);
	}

	// See services/api's CommerceMessagingConfiguration for why this
	// takes Spring Boot's ObjectMapper explicitly.
	@Bean
	MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}
}
