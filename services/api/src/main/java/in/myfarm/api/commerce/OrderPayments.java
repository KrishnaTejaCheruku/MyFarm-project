package in.myfarm.api.commerce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Small write port for the payment module to confirm or fail an
 * online payment, without depending on this package's package-private
 * entities/repositories directly -- same pattern as
 * catalog.VariantLookup / delivery.DeliveryAvailability, just in the
 * write direction.
 */
@Component
public class OrderPayments {

	private static final Logger log = LoggerFactory.getLogger(OrderPayments.class);

	private final OrderRepository orderRepository;

	OrderPayments(OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Transactional
	public void markPaid(String gatewayOrderId, String gatewayPaymentId) {
		orderRepository.findByGatewayOrderId(gatewayOrderId)
				.ifPresentOrElse(
						order -> order.markPaid(gatewayPaymentId),
						() -> log.warn(
								"Payment confirmation referenced unknown gateway order '{}'",
								gatewayOrderId));
	}

	@Transactional
	public void markPaymentFailed(String gatewayOrderId) {
		orderRepository.findByGatewayOrderId(gatewayOrderId)
				.ifPresentOrElse(
						OrderEntity::markPaymentFailed,
						() -> log.warn(
								"Payment failure referenced unknown gateway order '{}'",
								gatewayOrderId));
	}
}
