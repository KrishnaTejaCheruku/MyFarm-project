package in.myfarm.api.commerce;

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

	@Bean
	MessageConverter rabbitMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}
}
