package in.myfarm.api.commerce;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class CommerceMessagingConfiguration {

	// services/worker declares the matching queue + binding for this
	// exchange/routing key -- see worker's OrderEventsMessagingConfiguration.
	static final String ORDERS_EXCHANGE = "myfarm.orders";
	static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

	@Bean
	TopicExchange ordersExchange() {
		return new TopicExchange(ORDERS_EXCHANGE, true, false);
	}

	// Built from Spring Boot's own autoconfigured ObjectMapper (the same
	// one already serializing REST JSON responses) rather than
	// Jackson2JsonMessageConverter's no-arg constructor, which goes
	// through a Spring AMQP helper that references a newer Jackson class
	// than what actually resolves on this classpath.
	@Bean
	MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
		return new Jackson2JsonMessageConverter(objectMapper);
	}
}
