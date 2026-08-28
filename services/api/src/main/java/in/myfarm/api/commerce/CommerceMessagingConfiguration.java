package in.myfarm.api.commerce;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
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

	// This stack (Spring Boot 4.1 / Spring Framework 7 / Spring AMQP 4)
	// defaults to Jackson 3 (package tools.jackson.*), not classic
	// Jackson 2 (com.fasterxml.jackson.databind) -- Jackson2JsonMessageConverter
	// is deprecated for removal in favor of JacksonJsonMessageConverter,
	// which takes Jackson 3's JsonMapper. Built from Spring Boot's own
	// autoconfigured JsonMapper bean, the same one already serializing
	// REST JSON responses.
	@Bean
	MessageConverter rabbitMessageConverter(JsonMapper jsonMapper) {
		return new JacksonJsonMessageConverter(jsonMapper);
	}
}
